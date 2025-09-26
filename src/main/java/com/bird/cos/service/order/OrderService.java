package com.bird.cos.service.order;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.order.Order;
import com.bird.cos.domain.order.OrderItem;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductOption;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.order.DeliveryStatusCode;
import com.bird.cos.dto.order.OrderRequest;
import com.bird.cos.dto.order.OrderResponse;
import com.bird.cos.dto.order.OrderStatusCode;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.common.CommonCodeRepository;
import com.bird.cos.repository.order.OrderRepository;
import com.bird.cos.repository.product.ProductOptionRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    public OrderResponse createOrder(List<OrderRequest> orderItems) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        User getUser = userRepository.findByUserEmail(customUserDetails.getUserEmail())
                .orElseThrow(BusinessException::userNotFound);

        BigDecimal totalPrice = orderItems.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CommonCode code = commonCodeRepository.findById(OrderStatusCode.PENDING.getCode())
                .orElseThrow(BusinessException::codeNotFound);

        Order order = Order.builder()
                .user(getUser)
                .orderStatusCode(code)
                .orderDate(null) // 실제 주문시 저장
                .totalAmount(totalPrice) // 총 주문 금액
                .build();

        for (OrderRequest orderItem : orderItems) {
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> BusinessException.productNotFound(orderItem.getProductId()));

            ProductOption productOption = null;
            // 옵션이 있는 경우 옵션 검증
            if (orderItem.getProductOptionId() != null) {
                productOption = productOptionRepository.findById(orderItem.getProductOptionId())
                        .orElseThrow(BusinessException::optionNotFound);
                
                // 해당 옵션이 해당 상품의 옵션인지 검증
                if (!productOption.getProduct().getProductId().equals(product.getProductId())) {
                    throw BusinessException.optionBadRequest();
                }
            }

            CommonCode delCode = commonCodeRepository.findById(DeliveryStatusCode.PREPARING.getCode())
                    .orElseThrow(BusinessException::codeNotFound);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productOption(productOption)  // 옵션 설정 (null일 수 있음)
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .deliveryStatusCode(delCode)
                    .build();
            try {
                order.addOrderItem(item);
                log.info("Debug - After adding OrderItem: {}", item);
            } catch (Exception e) {
                log.error("Debug - Exception while adding OrderItem: {} / {}", e.getClass().getName(), e.getMessage());
            }
        }
        orderRepository.save(order);

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

        OrderResponse.UserResponse user = OrderResponse.UserResponse.builder()
                .userId(getUser.getUserId())
                .userName(getUser.getUserName())
                .userEmail(getUser.getUserEmail())
                .address(getUser.getUserAddress())
                .phone(getUser.getUserPhone())
                .build();

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .items(itemResponses)
                .user(user)
                .totalPrice(totalPrice)
                .totalAmount(totalPrice) // 현재는 쿠폰/포인트 할인이 없으므로 totalPrice와 동일
                .build();
    }
}
