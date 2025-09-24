package com.bird.cos.dto.mypage;

import com.bird.cos.domain.brand.Brand;
import com.bird.cos.domain.coupon.Coupon;
import com.bird.cos.domain.coupon.CouponScope;
import com.bird.cos.domain.product.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CouponResponse {

    private final Long couponId;
    private final CouponScope couponScope;
    private final String couponTitle;
    private final String couponDescription;
    private final BigDecimal discountRate;
    private final BigDecimal discountAmount;
    private final BigDecimal maxDiscountAmount;
    private final BigDecimal minPurchaseAmount;
    private final String couponImageUrl;
    private final LocalDateTime startDate;
    private final LocalDateTime expiredAt;
    private final Boolean active;
    private final Long brandId;
    private final String brandName;
    private final Long productId;
    private final String productTitle;

    public static CouponResponse from(Coupon coupon) {
        Brand brand = coupon.getBrand();
        Product product = coupon.getProduct();

        return CouponResponse.builder()
                .couponId(coupon.getCouponId())
                .couponScope(coupon.getScope())
                .couponTitle(coupon.getCouponTitle())
                .couponDescription(coupon.getCouponDescription())
                .discountRate(coupon.getDiscountRate())
                .discountAmount(coupon.getDiscountAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minPurchaseAmount(coupon.getMinPurchaseAmount())
                .couponImageUrl(coupon.getCouponImageUrl())
                .startDate(coupon.getStartDate())
                .expiredAt(coupon.getExpiredAt())
                .active(coupon.getIsActive())
                .brandId(brand != null ? brand.getBrandId() : null)
                .brandName(brand != null ? brand.getBrandName() : null)
                .productId(product != null ? product.getProductId() : null)
                .productTitle(product != null ? product.getProductTitle() : null)
                .build();
    }
}
