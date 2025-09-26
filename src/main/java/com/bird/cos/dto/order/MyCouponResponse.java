package com.bird.cos.dto.order;

import com.bird.cos.domain.coupon.UserCoupon;
import com.bird.cos.dto.mypage.CouponResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyCouponResponse {

    private final Long userCouponId;
    private final CouponResponse coupon;
    private final LocalDateTime issuedAt;
    private final LocalDateTime usedAt;
    private final boolean usable;
    private final boolean expired;

    public static MyCouponResponse from(UserCoupon userCoupon) {

        LocalDateTime now = LocalDateTime.now();
        boolean expired = userCoupon.getCoupon().getExpiredAt().isBefore(now) || Boolean.FALSE.equals(userCoupon.getCoupon().getIsActive());
        boolean usable = userCoupon.getUsedAt() == null && !expired;

        return MyCouponResponse.builder()
                .userCouponId(userCoupon.getUserCouponId())
                .coupon(CouponResponse.from(userCoupon.getCoupon()))
                .issuedAt(userCoupon.getIssuedAt())
                .usedAt(userCoupon.getUsedAt())
                .usable(usable)
                .expired(expired)
                .build();
    }
}
