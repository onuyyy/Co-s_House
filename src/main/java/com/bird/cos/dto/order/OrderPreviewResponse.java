package com.bird.cos.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class OrderPreviewResponse {

    private List<OrderItemPreviewResponse> items;
    private BigDecimal totalPrice;
    private BigDecimal totalAmount; // 최종 결제 금액 (쿠폰, 포인트 할인 적용 후)
    private UserPreviewResponse user;

    @Getter
    @Builder
    public static class UserPreviewResponse {
        private Long userId;
        private String userName;
        private String userEmail;
        private String address;
        private String phone;
    }

    @Getter
    @Builder
    public static class OrderItemPreviewResponse {
        private Long productId;
        private String productName;
        private Long productOptionId;
        private String productOptionName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal itemTotalPrice; // price * quantity
        private String imageUrl;
        private Long cartItemId;
    }
}
