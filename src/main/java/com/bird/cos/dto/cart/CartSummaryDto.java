package com.bird.cos.dto.cart;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

// 장바구니 요약 DTO - 총 수량, 총 금액, 가격변동/품절 카운트 등 요약정보
@Getter
@Builder
public class CartSummaryDto {
    private Integer totalQuantity; //총수량
    private BigDecimal totalAmount; //총금액
    private BigDecimal expectedDiscount; //예상 할인 금액
    private BigDecimal expectedAmount; //예상 결제 금액
    private Integer changedCount; //가격변동
    private Integer outOfStockCount; //품절
}
