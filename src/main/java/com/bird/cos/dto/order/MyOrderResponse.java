package com.bird.cos.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyOrderResponse {

    private Long orderId;
    private LocalDateTime orderDate;
    private String orderStatus;           // 주문 상태 (결제완료, 배송중 등)
    private String orderStatusCode;       // 주문 상태 코드
    private BigDecimal totalAmount;       // 총 주문 금액
    private BigDecimal paidAmount;        // 실제 결제 금액
    private LocalDateTime confirmedDate;  // 구매확정일

    private List<MyOrderItemResponse> items;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyOrderItemResponse {
        private Long orderItemId;
        private Long productId;
        private String productName;
        private String productImage;
        private String productOptionName;   // 옵션명 (옵션명 : 옵션값)
        private Integer quantity;
        private BigDecimal price;
        private String deliveryStatus;      // 배송 상태
        private String deliveryStatusCode;  // 배송 상태 코드
    }
}
