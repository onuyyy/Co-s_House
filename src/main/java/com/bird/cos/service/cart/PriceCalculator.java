package com.bird.cos.service.cart;

import com.bird.cos.domain.product.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;

/* 참고사항
 * 장바구니 가격 계산 유틸리티
 * - 쿠폰/세일/할인율/정가 중 가장 유리한 단가를 선택
 * - 라인 합계(단가 × 수량) 자동 계산
 */
public final class PriceCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int SCALE_MONEY = 0; // 금액 소수 자릿수(카트는 원 단위, HALF_UP)
    private static final int SCALE_RATE_CALC = 10; // 할인율 계산 중간 스케일

    private PriceCalculator() {}

    /**
     * 적용 단가 계산
     * 규칙: coupon_price, sale_price, original_price×(1−discount_rate/100), original_price
     *      중 null이 아니고 0보다 큰 값들 중 최솟값을 선택
     */
    public static BigDecimal effectiveUnitPrice(Product p) {
        if (p == null) return null;

        BigDecimal original = safeScale(p.getOriginalPrice());
        BigDecimal sale = safeScale(p.getSalePrice());
        BigDecimal coupon = safeScale(p.getCouponPrice());
        BigDecimal discounted = discountedOriginal(original, safeScale(p.getDiscountRate()));

        BigDecimal effective = minPositive(coupon, sale, discounted, original);
        return effective != null ? effective : null;
    }
    //라인 합계 계산(단가 × 수량) - 원 단위(소수 0자리) HALF_UP 반올림
    public static BigDecimal lineTotal(BigDecimal unitPrice, int quantity) {
        if (unitPrice == null) return null;
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    private static BigDecimal discountedOriginal(BigDecimal original, BigDecimal discountRate) {
        if (original == null || discountRate == null) return null;
        if (isZeroOrNegative(discountRate)) return original;
        BigDecimal multiplier = HUNDRED.subtract(discountRate).divide(HUNDRED, SCALE_RATE_CALC, RoundingMode.HALF_UP);
        return original.multiply(multiplier).setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    private static BigDecimal minPositive(BigDecimal... values) {
        BigDecimal min = null;
        for (BigDecimal v : values) {
            if (v == null || isZeroOrNegative(v)) continue;
            if (min == null || v.compareTo(min) < 0) {
                min = v;
            }
        }
        return min;
    }

    private static BigDecimal safeScale(BigDecimal value) {
        return value == null ? null : value.setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    private static boolean isZeroOrNegative(BigDecimal v) {
        return v == null || v.compareTo(BigDecimal.ZERO) <= 0;
    }
}
