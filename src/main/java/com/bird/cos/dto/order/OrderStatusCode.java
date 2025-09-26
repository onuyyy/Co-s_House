package com.bird.cos.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderStatusCode {

    PENDING("ORDER_001", "결제 대기"),
    PAID("ORDER_002", "결제 완료"),
    PREPARING("ORDER_003", "배송 준비중"),
    SHIPPING("ORDER_004", "배송중"),
    DELIVERED("ORDER_005", "배송 완료"),
    CONFIRMED("ORDER_006", "구매 확정"),
    CANCELLED("ORDER_007", "주문 취소"),
    REFUNDED("ORDER_008", "환불 완료");

    private final String code;        // CommonCode.code
    private final String description; // 상태 설명
}
