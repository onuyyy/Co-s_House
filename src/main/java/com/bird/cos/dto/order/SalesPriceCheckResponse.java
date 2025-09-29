package com.bird.cos.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SalesPriceCheckResponse {
    // 할인율
    private BigDecimal discountRate;
    // 할인 금액
    private BigDecimal discountAmount;
    // 최대 할인 금액
    private BigDecimal maxDiscountAmount;
    // 최소 주문 금액
    private BigDecimal minPurchaseAmount;
    // 실제 계산 금액
    private BigDecimal calculatedDiscountAmount;
    // 실제 계산 금액
    private BigDecimal finalAmount;
    // 적용 가능 여부
    private boolean applicable;
}
