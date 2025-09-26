package com.bird.cos.dto.events;

import com.bird.cos.domain.coupon.Coupon;
import lombok.Builder;
import lombok.Getter;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class EventCouponInfo {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final Long couponId;
    private final String title;
    private final String description;
    private final String discountText;
    private final String validDateText;
    private final boolean alreadyClaimed;

    public static EventCouponInfo from(Coupon coupon) {
        return from(coupon, false);
    }

    public static EventCouponInfo from(Coupon coupon, boolean alreadyClaimed) {
        if (coupon == null) {
            throw new IllegalArgumentException("coupon must not be null");
        }

        return EventCouponInfo.builder()
                .couponId(coupon.getCouponId())
                .title(coupon.getCouponTitle())
                .description(coupon.getCouponDescription())
                .validDateText(formatPeriod(coupon))
                .alreadyClaimed(alreadyClaimed)
                .build();
    }


    private static String formatPeriod(Coupon coupon) {
        if (coupon.getStartDate() == null || coupon.getExpiredAt() == null) {
            return "상시";
        }
        return PERIOD_FORMATTER.format(coupon.getStartDate()) + " ~ " +
                PERIOD_FORMATTER.format(coupon.getExpiredAt());
    }
}
