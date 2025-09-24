package com.bird.cos.service.mypage;

import com.bird.cos.domain.coupon.Coupon;
import com.bird.cos.domain.coupon.CouponScope;
import com.bird.cos.domain.user.User;
import com.bird.cos.domain.coupon.UserCoupon;
import com.bird.cos.dto.mypage.CouponResponse;
import com.bird.cos.dto.mypage.UserCouponResponse;
import com.bird.cos.repository.mypage.CouponRepository;
import com.bird.cos.repository.mypage.CouponSpecifications;
import com.bird.cos.repository.mypage.UserCouponRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;

    public Page<CouponResponse> searchCoupons(Long userId,
                                              CouponScope scope,
                                              Long brandId,
                                              String status,
                                              String keyword,
                                              Pageable pageable) {

        Specification<Coupon> spec = Specification.where(null);

        if (scope != null) {
            spec = spec.and(CouponSpecifications.hasScope(scope));
        }
        if (brandId != null) {
            spec = spec.and(CouponSpecifications.hasBrand(brandId));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and(CouponSpecifications.containsKeyword(keyword.trim()));
        }
        if (status != null) {
            switch (status.toLowerCase()) {
                case "active" -> spec = spec.and(CouponSpecifications.isActive(true));
                case "expired" -> spec = spec.and(CouponSpecifications.isActive(false));
            }
        }
        if (userId != null) {
            spec = spec.and(CouponSpecifications.notClaimedBy(userId));
        }

        return couponRepository.findAll(spec, pageable).map(CouponResponse::from);
    }

    public List<CouponResponse> findAll() {
        return couponRepository.findAll().stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void claimCoupon(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        if (Boolean.FALSE.equals(coupon.getIsActive()) || coupon.getExpiredAt().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("만료되었거나 비활성화된 쿠폰입니다.");
        }

        if (userCouponRepository.existsByUser_UserIdAndCoupon_CouponId(userId, couponId)) {
            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .build();

        userCouponRepository.save(userCoupon);
    }

    @Transactional(readOnly = true)
    public Page<UserCouponResponse> findUserCoupons(Long userId, Pageable pageable) {
        return userCouponRepository.findByUser_UserId(userId, pageable)
                .map(UserCouponResponse::from);
    }
}
