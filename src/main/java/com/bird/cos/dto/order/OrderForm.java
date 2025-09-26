package com.bird.cos.dto.order;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class OrderForm {
    private List<OrderRequest> orderItems = new ArrayList<>(); // List<OrderRequest> 담기

    // 쿠폰 정보
    private Long userCouponId; // 사용한 쿠폰 ID
    private BigDecimal couponDiscountAmount = BigDecimal.ZERO; // 쿠폰 할인 금액

    // 포인트 정보
    private BigDecimal usedPoints = BigDecimal.ZERO; // 사용한 포인트

    // 최종 결제 금액
    private BigDecimal finalAmount = BigDecimal.ZERO; // 쿠폰/포인트 적용 후 실제 결제금액
}
