package com.bird.cos.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Long orderId;
    private List<OrderItemResponse> items;
    private BigDecimal totalPrice;
    private BigDecimal totalAmount; // 최종 결제 금액 (쿠폰, 포인트 할인 적용 후)
    private UserResponse user;

    @Getter
    @Builder
    public static class UserResponse {
        private Long userId;
        private String userName;
        private String userEmail;
        private String address;
        private String phone;
    }

    @Getter
    @Builder
    public static class OrderItemResponse {
        private Long orderItemId;
        private Long orderId;
        private Long productId;
        private String productName;
        private Long productOptionId;
        private String productOptionName;
        private Integer quantity;
        private BigDecimal price;
        private String imageUrl;
    }

}
