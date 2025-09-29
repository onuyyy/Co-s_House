package com.bird.cos.service.order;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.coupon.Coupon;
import com.bird.cos.domain.coupon.UserCoupon;
import com.bird.cos.domain.order.Order;
import com.bird.cos.domain.order.OrderItem;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductOption;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.mypage.MyOrderRequest;
import com.bird.cos.dto.order.*;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.common.CommonCodeRepository;
import com.bird.cos.repository.mypage.coupon.UserCouponRepository;
import com.bird.cos.repository.order.OrderRepository;
import com.bird.cos.repository.product.ProductOptionRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.service.user.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final UserCouponRepository userCouponRepository;
    private final PointService pointService;

    @Transactional(readOnly = true)
    public OrderPreviewResponse getOrderPreview(String email, List<OrderRequest> orderItems) {
        User user = getUserByEmail(email);

        List<OrderPreviewResponse.OrderItemPreviewResponse> itemPreviews = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderRequest orderItem : orderItems) {
            Product product = getProductById(orderItem.getProductId());
            ProductOption productOption = validateAndGetProductOption(orderItem, product);

            String productOptionName = productOption != null
                ? productOption.getOptionName() + " : " + productOption.getOptionValue()
                : null;

            BigDecimal itemTotalPrice = calculateItemTotalPrice(orderItem);
            totalPrice = totalPrice.add(itemTotalPrice);

            OrderPreviewResponse.OrderItemPreviewResponse itemPreview = OrderPreviewResponse.OrderItemPreviewResponse.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductTitle())
                    .productOptionId(productOption != null ? productOption.getOptionId() : null)
                    .productOptionName(productOptionName)
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .itemTotalPrice(itemTotalPrice)
                    .build();

            itemPreviews.add(itemPreview);
        }

        OrderPreviewResponse.UserPreviewResponse userPreview = createUserPreviewResponse(user);

        return OrderPreviewResponse.builder()
                .items(itemPreviews)
                .user(userPreview)
                .totalPrice(totalPrice)
                .totalAmount(totalPrice) // 현재는 쿠폰/포인트 할인이 없으므로 totalPrice와 동일
                .build();
    }

    // 쿠폰/포인트를 포함한 주문 생성
    public OrderResponse createOrder(String email, List<OrderRequest> orderItems,
                                   Long userCouponId, BigDecimal couponDiscountAmount,
                                   BigDecimal usedPoints, BigDecimal finalAmount) {

        User user = getUserByEmail(email);
        BigDecimal totalPrice = calculateTotalPrice(orderItems);

        CommonCode orderStatusCode = commonCodeRepository.findById(OrderStatusCode.PENDING.getCode())
                .orElseThrow(BusinessException::codeNotFound);

        // Order를 저장하여 ID를 생성
        Order order = Order.builder()
                .user(user)
                .orderStatusCode(orderStatusCode)
                .orderDate(LocalDateTime.now()) // 주문 생성 시점으로 설정 (구매 신청일시)
                .totalAmount(totalPrice) // 총 주문 금액 (할인 전)
                .paidAmount(finalAmount != null ? finalAmount : totalPrice) // 실제 결제 금액 (할인 후)
                .build();

        order = orderRepository.save(order);

        // Order가 저장된 후 OrderItem들 추가
        addOrderItemsToOrder(order, orderItems);

        // OrderItem 들이 추가된 Order를 다시 저장
        order = orderRepository.save(order);

        // 쿠폰 사용 처리
        if (userCouponId != null) {
            useMyCoupon(userCouponId);
        }

        // 포인트 사용 처리
        if (usedPoints != null && usedPoints.compareTo(BigDecimal.ZERO) > 0) {
            log.info("Debug - Processing point usage: usedPoints={}", usedPoints);
            try {
                // 포인트 사용 처리 (UserPoint 차감 + PointHistory 저장)
                pointService.useOrderPoints(user.getUserId(), usedPoints.intValue(), order.getOrderId().toString());
                log.info("Debug - Point usage completed: userId={}, usedPoints={}, orderId={}",
                        user.getUserId(), usedPoints, order.getOrderId());
            } catch (Exception e) {
                log.error("Debug - Point usage failed: userId={}, usedPoints={}, error={}",
                        user.getUserId(), usedPoints, e.getMessage());
                throw BusinessException.pointUsageFailed(user.getUserId(), usedPoints.intValue());
            }
        }

        List<OrderResponse.OrderItemResponse> itemResponses = createOrderItemResponses(order);
        OrderResponse.UserResponse userResponse = createUserResponse(user);

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .items(itemResponses)
                .user(userResponse)
                .totalPrice(totalPrice) // 할인 전 총 금액
                .totalAmount(finalAmount != null ? finalAmount : totalPrice) // 할인 후 실제 결제 금액
                .build();
    }

    /**
     * 이메일로 사용자 조회
     */
    private User getUserByEmail(String email) {
        return userRepository.findByUserEmail(email)
                .orElseThrow(BusinessException::userNotFound);
    }

    /**
     * 상품 ID로 상품 조회
     */
    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.productNotFound(productId));
    }

    /**
     * 상품 옵션 검증 및 조회
     */
    private ProductOption validateAndGetProductOption(OrderRequest orderItem, Product product) {
        if (orderItem.getProductOptionId() == null) {
            return null;
        }

        ProductOption productOption = productOptionRepository.findById(orderItem.getProductOptionId())
                .orElseThrow(BusinessException::optionNotFound);

        // 해당 옵션이 해당 상품의 옵션인지 검증
        if (!productOption.getProduct().getProductId().equals(product.getProductId())) {
            throw BusinessException.optionBadRequest();
        }

        return productOption;
    }

    /**
     * 주문 아이템의 총 가격 계산
     */
    private BigDecimal calculateItemTotalPrice(OrderRequest orderItem) {
        return orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
    }

    /**
     * 전체 주문의 총 가격 계산
     */
    private BigDecimal calculateTotalPrice(List<OrderRequest> orderItems) {
        return orderItems.stream()
                .map(this::calculateItemTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 주문에 주문 아이템들 추가
     */
    private void addOrderItemsToOrder(Order order, List<OrderRequest> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            log.warn("Debug - orderItems is null or empty!");
            return;
        }

        CommonCode deliveryStatusCode = commonCodeRepository.findById(DeliveryStatusCode.PREPARING.getCode())
                .orElseThrow(BusinessException::codeNotFound);

        for (int i = 0; i < orderItems.size(); i++) {
            OrderRequest orderItem = orderItems.get(i);
            log.info("Debug - Processing orderItem[{}]: productId={}, quantity={}, price={}",
                i, orderItem.getProductId(), orderItem.getQuantity(), orderItem.getPrice());

            Product product = getProductById(orderItem.getProductId());
            ProductOption productOption = validateAndGetProductOption(orderItem, product);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productOption(productOption)
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .deliveryStatusCode(deliveryStatusCode)
                    .build();

            try {
                order.addOrderItem(item);
            } catch (Exception e) {
                throw e; // 예외를 다시 던져서 실패를 명확히 함
            }
        }
    }

    /**
     * 주문 아이템 응답 객체 생성
     */
    private List<OrderResponse.OrderItemResponse> createOrderItemResponses(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItem item : order.getOrderItems()) {
            OrderResponse.OrderItemResponse itemResponse = OrderResponse.OrderItemResponse.builder()
                    .orderItemId(item.getOrderItemId())
                    .orderId(order.getOrderId())
                    .productId(item.getProduct().getProductId())
                    .productOptionId(item.getProductOption() != null ? item.getProductOption().getOptionId() : null)
                    .productOptionName(item.getProductOption() != null ?
                            item.getProductOption().getOptionName() + " : " + item.getProductOption().getOptionValue() : null)
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .build();

            itemResponses.add(itemResponse);
        }

        return itemResponses;
    }

    /**
     * 사용자 응답 객체 생성 (주문 완료용)
     */
    private OrderResponse.UserResponse createUserResponse(User user) {
        return OrderResponse.UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .address(user.getUserAddress())
                .phone(user.getUserPhone())
                .build();
    }

    /**
     * 사용자 미리보기 응답 객체 생성 (주문 미리보기용)
     */
    private OrderPreviewResponse.UserPreviewResponse createUserPreviewResponse(User user) {
        return OrderPreviewResponse.UserPreviewResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .address(user.getUserAddress())
                .phone(user.getUserPhone())
                .build();
    }

    /**
     * 유저 쿠폰 사용 처리
     * @param userCouponId 사용할 유저 쿠폰 ID
     */
    public void useMyCoupon(Long userCouponId) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> BusinessException.couponNotFound(userCouponId));

        // 쿠폰 유효성 검증
        validateCouponUsability(userCoupon);

        // 쿠폰 상태가 null인 경우 ISSUED 상태로 설정 (데이터 무결성 보장)
        if (userCoupon.getCouponStatus() == null) {
            CommonCode issuedStatus = commonCodeRepository.findById("COUPON_001")
                    .orElseThrow(BusinessException::codeNotFound);
            userCoupon.setCouponStatus(issuedStatus);
            log.info("Debug - Set default coupon status to ISSUED for userCouponId: {}", userCouponId);
        }

        // 쿠폰을 사용됨 상태로 변경
        CommonCode usedStatus = commonCodeRepository.findById("COUPON_002")
                .orElseThrow(BusinessException::codeNotFound);

        userCoupon.setCouponStatus(usedStatus);
        userCoupon.setUsedAt(LocalDateTime.now());

        userCouponRepository.save(userCoupon);

        log.info("Debug - Coupon usage completed: userCouponId={}, couponId={}, userId={}",
                userCouponId, userCoupon.getCoupon().getCouponId(), userCoupon.getUser().getUserId());
    }

    /**
     * 쿠폰 사용 가능 여부 검증
     * @param userCoupon 검증할 유저 쿠폰
     */
    private void validateCouponUsability(UserCoupon userCoupon) {
        Coupon coupon = userCoupon.getCoupon();

        // 1. 쿠폰이 활성화 상태인지 확인
        if (coupon.getIsActive() == null || !coupon.getIsActive()) {
            throw BusinessException.couponExpired(coupon.getCouponId());
        }

        // 2. 쿠폰 유효기간 확인
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getExpiredAt())) {
            throw BusinessException.couponExpired(coupon.getCouponId());
        }

        // 3. 이미 사용된 쿠폰인지 확인
        if (userCoupon.getUsedAt() != null) {
            throw BusinessException.couponUsed(userCoupon.getUserCouponId());
        }

        // 4. 쿠폰 상태 확인 (null인 경우 ISSUED로 간주)
        CommonCode couponStatus = userCoupon.getCouponStatus();
        if (couponStatus != null && !"COUPON_001".equals(couponStatus.getCodeId())) {
            // 상태가 있지만 ISSUED가 아닌 경우
            throw BusinessException.couponUsed(userCoupon.getUserCouponId());
        }
        // couponStatus가 null인 경우는 기본적으로 ISSUED 상태로 간주하여 통과
    }

    /**
     * 구매 확정 처리 - confirmedDate 설정
     * @param orderId 주문 ID
     * @param userEmail 사용자 이메일 (권한 검증용)
     * @return 구매확정 성공 여부
     */
    public boolean confirmOrder(Long orderId, String userEmail) {

        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BusinessException.orderNotFound(orderId));

        // 주문자 권한 검증
        if (!order.getUser().getUserEmail().equals(userEmail)) {
            throw BusinessException.orderAccessDenied();
        }

        // 이미 구매확정된 주문인지 검증
        if (order.getConfirmedDate() != null) {
            log.warn("Debug - 주문이 이미 구매 확정되었습니다. : orderId={}, confirmedDate={}",
                orderId, order.getConfirmedDate());
            return false; // 이미 구매확정됨
        }

        // 구매확정 처리 (confirmedDate 설정)
        // Order 엔티티에 setter나 confirm 메서드가 필요
        // order.confirm(LocalDateTime.now()); // 이런 방식으로 구현 권장

        // 임시로 repository에서 직접 업데이트 (실제로는 Order 엔티티에 메서드 추가 권장)
        order = Order.builder()
                .orderId(order.getOrderId())
                .user(order.getUser())
                .orderStatusCode(order.getOrderStatusCode())
                .totalAmount(order.getTotalAmount())
                .paidAmount(order.getPaidAmount())
                .orderDate(order.getOrderDate())
                .confirmedDate(LocalDateTime.now()) // 구매확정 일시 설정
                .build();

        // 주문 상태를 '완료'로 변경

        orderRepository.save(order);
        log.info("Debug - Order confirmed successfully: orderId={}, confirmedDate={}",
            orderId, order.getConfirmedDate());

        // 추가 처리 (포인트 지급, 알림 발송 등)
        // TODO: 구매확정 시 포인트 지급 로직
        // TODO: 구매확정 알림 발송

        return true;
    }

    /**
     * 사용자의 주문 내역 조회 (페이징, 검색)
     * @param userId 사용자 ID
     * @param request 검색 조건
     * @param pageable 페이징 정보
     * @return 주문 내역 페이지
     */
    @Transactional(readOnly = true)
    public Page<MyOrderResponse> getMyOrders(Long userId, MyOrderRequest request, Pageable pageable) {
        // request가 null이거나 모든 조건이 비어있으면 기본 조회
        if (request == null || isEmptySearchCondition(request)) {
            Page<Order> orderPage = orderRepository.findByUserIdOrderByOrderDateDesc(userId, pageable);

            List<Long> orderIds = orderPage.getContent().stream()
                    .map(Order::getOrderId)
                    .toList();

            List<Order> ordersWithDetails = orderIds.isEmpty() ? 
                    List.of() : 
                    orderRepository.findByOrderIdsWithDetails(orderIds);

            java.util.Map<Long, Order> orderMap = ordersWithDetails.stream()
                    .collect(java.util.stream.Collectors.toMap(Order::getOrderId, o -> o));

            List<MyOrderResponse> responses = orderPage.getContent().stream()
                    .map(o -> convertToMyOrderResponse(orderMap.get(o.getOrderId())))
                    .toList();
            
            return new PageImpl<>(
                    responses, 
                    pageable, 
                    orderPage.getTotalElements()
            );
        }

        Page<Order> orderPage = orderRepository.searchOrders(userId, request, pageable);
        
        List<MyOrderResponse> responses = orderPage.getContent().stream()
                .map(this::convertToMyOrderResponse)
                .toList();
        
        return new PageImpl<>(
                responses, 
                pageable, 
                orderPage.getTotalElements()
        );
    }

    /**
     * 검색 조건이 비어있는지 확인
     */
    private boolean isEmptySearchCondition(MyOrderRequest request) {
        return (request.getSearchDate() == null || request.getSearchDate() == com.bird.cos.dto.mypage.SearchDate.ALL) &&
               (request.getOrderStatus() == null || request.getOrderStatus().isEmpty() || request.getOrderStatus().equals("ALL")) &&
               (request.getSearchValue() == null || request.getSearchValue().trim().isEmpty());
    }

    /**
     * Order 엔티티를 MyOrderResponse로 변환
     */
    private MyOrderResponse convertToMyOrderResponse(Order order) {
        List<MyOrderResponse.MyOrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::convertToMyOrderItemResponse)
                .toList();

        return MyOrderResponse.builder()
                .orderId(order.getOrderId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatusCode().getDescription())
                .orderStatusCode(order.getOrderStatusCode().getCodeId())
                .totalAmount(order.getTotalAmount())
                .paidAmount(order.getPaidAmount())
                .confirmedDate(order.getConfirmedDate())
                .items(itemResponses)
                .build();
    }

    /**
     * OrderItem 엔티티를 MyOrderItemResponse로 변환
     */
    private MyOrderResponse.MyOrderItemResponse convertToMyOrderItemResponse(OrderItem orderItem) {
        String productOptionName = null;
        if (orderItem.getProductOption() != null) {
            productOptionName = orderItem.getProductOption().getOptionName() + " : " +
                    orderItem.getProductOption().getOptionValue();
        }

        // 상품 대표 이미지 가져오기
        String productImage = null;
        if (orderItem.getProduct().getMainImageUrl() != null && !orderItem.getProduct().getMainImageUrl().isEmpty()) {
            productImage = orderItem.getProduct().getMainImageUrl();
        }

        return MyOrderResponse.MyOrderItemResponse.builder()
                .orderItemId(orderItem.getOrderItemId())
                .productId(orderItem.getProduct().getProductId())
                .productName(orderItem.getProduct().getProductTitle())
                .productImage(productImage)
                .productOptionName(productOptionName)
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .deliveryStatus(orderItem.getDeliveryStatusCode().getDescription())
                .deliveryStatusCode(orderItem.getDeliveryStatusCode().getCodeId())
                .build();
    }
}
