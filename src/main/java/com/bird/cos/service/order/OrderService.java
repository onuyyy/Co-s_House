package com.bird.cos.service.order;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.order.Order;
import com.bird.cos.domain.order.OrderItem;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductOption;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.order.*;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.common.CommonCodeRepository;
import com.bird.cos.repository.order.OrderRepository;
import com.bird.cos.repository.product.ProductOptionRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // 기존 메서드 호환성을 위한 오버로드
    public OrderResponse createOrder(String email, List<OrderRequest> orderItems) {
        return createOrder(email, orderItems, null, BigDecimal.ZERO, BigDecimal.ZERO, null);
    }

    // 쿠폰/포인트를 포함한 주문 생성
    public OrderResponse createOrder(String email, List<OrderRequest> orderItems,
                                   Long userCouponId, BigDecimal couponDiscountAmount,
                                   BigDecimal usedPoints, BigDecimal finalAmount) {
        log.info("Debug - createOrder called with email: {}, orderItems size: {}, couponId: {}, discount: {}, points: {}, finalAmount: {}",
            email, orderItems != null ? orderItems.size() : "null", userCouponId, couponDiscountAmount, usedPoints, finalAmount);

        User user = getUserByEmail(email);
        BigDecimal totalPrice = calculateTotalPrice(orderItems);

        CommonCode orderStatusCode = commonCodeRepository.findById(OrderStatusCode.PENDING.getCode())
                .orElseThrow(BusinessException::codeNotFound);

        // 1. 먼저 Order를 저장하여 ID를 생성
        Order order = Order.builder()
                .user(user)
                .orderStatusCode(orderStatusCode)
                .orderDate(LocalDateTime.now()) // 주문 생성 시점으로 설정 (구매 신청일시)
                .totalAmount(totalPrice) // 총 주문 금액 (할인 전)
                .paidAmount(finalAmount != null ? finalAmount : totalPrice) // 실제 결제 금액 (할인 후)
                .build();

        order = orderRepository.save(order); // Order 먼저 저장
        log.info("Debug - Order saved with ID: {}, totalAmount: {}, paidAmount: {}",
            order.getOrderId(), order.getTotalAmount(), order.getPaidAmount());

        // 2. Order가 저장된 후 OrderItem들 추가
        addOrderItemsToOrder(order, orderItems);

        // 3. OrderItem들이 추가된 Order를 다시 저장
        order = orderRepository.save(order);
        log.info("Debug - Order saved again after adding items. Final OrderItems count: {}", order.getOrderItems().size());

        // 4. 쿠폰 사용 처리 (쿠폰을 사용한 경우)
        if (userCouponId != null) {
            log.info("Debug - Processing coupon usage: userCouponId={}", userCouponId);
            // TODO: 쿠폰 사용 처리 로직 (쿠폰을 사용됨 상태로 변경)
        }

        // 5. 포인트 사용 처리 (포인트를 사용한 경우)
        if (usedPoints != null && usedPoints.compareTo(BigDecimal.ZERO) > 0) {
            log.info("Debug - Processing point usage: usedPoints={}", usedPoints);
            // TODO: 포인트 차감 로직 (사용자 포인트에서 차감)
        }

        List<OrderResponse.OrderItemResponse> itemResponses = createOrderItemResponses(order);
        OrderResponse.UserResponse userResponse = createUserResponse(user);

        log.info("Debug - Response created with {} itemResponses", itemResponses.size());

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
        log.info("Debug - addOrderItemsToOrder called with orderItems size: {}", orderItems != null ? orderItems.size() : "null");

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
                log.info("Debug - Successfully added OrderItem[{}] to Order. Order now has {} items",
                    i, order.getOrderItems().size());
            } catch (Exception e) {
                log.error("Debug - Exception while adding OrderItem[{}]: {} / {}",
                    i, e.getClass().getName(), e.getMessage());
                throw e; // 예외를 다시 던져서 실패를 명확히 함
            }
        }

        log.info("Debug - Finished adding all OrderItems. Final count: {}", order.getOrderItems().size());
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

        // 주문 상태를 '완료'로 변경할 수도 있음
        // CommonCode completedStatus = commonCodeRepository.findById(OrderStatusCode.COMPLETED.getCode())...

        orderRepository.save(order);
        log.info("Debug - Order confirmed successfully: orderId={}, confirmedDate={}",
            orderId, order.getConfirmedDate());

        // 추가 처리 (포인트 지급, 알림 발송 등)
        // TODO: 구매확정 시 포인트 지급 로직
        // TODO: 구매확정 알림 발송

        return true;
    }
}
