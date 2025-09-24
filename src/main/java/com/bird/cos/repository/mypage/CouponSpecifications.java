package com.bird.cos.repository.mypage;

import com.bird.cos.domain.coupon.Coupon;
import com.bird.cos.domain.coupon.CouponScope;
import com.bird.cos.domain.coupon.UserCoupon;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class CouponSpecifications {

    private CouponSpecifications() {
    }

    public static Specification<Coupon> hasScope(CouponScope scope) {
        return (root, query, builder) -> builder.equal(root.get("scope"), scope);
    }

    public static Specification<Coupon> hasBrand(Long brandId) {
        return (root, query, builder) -> builder.equal(root.get("brand").get("brandId"), brandId);
    }

    public static Specification<Coupon> containsKeyword(String keyword) {
        String like = "%" + keyword.toLowerCase() + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("couponTitle")), like),
                builder.like(builder.lower(root.get("couponDescription")), like)
        );
    }

    public static Specification<Coupon> isActive(boolean active) {
        LocalDateTime now = LocalDateTime.now();
        return (root, query, builder) -> active
                ? builder.and(
                        builder.equal(root.get("isActive"), true),
                        builder.greaterThan(root.get("expiredAt"), now)
                )
                : builder.or(
                        builder.equal(root.get("isActive"), false),
                        builder.lessThanOrEqualTo(root.get("expiredAt"), now)
                );
    }

    public static Specification<Coupon> notClaimedBy(Long userId) {
        return (root, query, builder) -> {
            Subquery<UserCoupon> subQuery = query.subquery(UserCoupon.class);
            var userCouponRoot = subQuery.from(UserCoupon.class);
            subQuery.select(userCouponRoot)
                    .where(
                            builder.equal(userCouponRoot.get("coupon"), root),
                            builder.equal(userCouponRoot.get("user").get("userId"), userId)
                    );
            return builder.not(builder.exists(subQuery));
        };
    }
}
