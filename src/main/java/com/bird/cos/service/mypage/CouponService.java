package com.bird.cos.service.mypage;

import com.bird.cos.domain.coupon.Coupon;
import com.bird.cos.domain.coupon.CouponScope;
import com.bird.cos.domain.coupon.UserCoupon;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.mypage.CouponResponse;
import com.bird.cos.dto.mypage.UserCouponResponse;
import com.bird.cos.dto.order.MyCouponResponse;
import com.bird.cos.dto.order.SalesPriceCheckResponse;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.mypage.coupon.CouponRepository;
import com.bird.cos.repository.mypage.coupon.CouponSpecifications;
import com.bird.cos.repository.mypage.coupon.UserCouponRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;
    private final ProductRepository productRepository;

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

    public List<MyCouponResponse> getMyCoupons(Long userId) {
        return userCouponRepository.findByUser_UserIdAndCoupon_ExpiredAtAfterAndCoupon_IsActive(
                userId, LocalDateTime.now(), true).stream().map(MyCouponResponse::from).toList();
    }

    /**
     * 해당하는 쿠폰의 할인율 할인금액 최소주문금액 최대할인금액 검증 및 계산
     * @param couponId 쿠폰 ID
     * @param orderAmount 주문 금액 (배송비 포함)
     * @return 쿠폰 적용 결과
     */
    public SalesPriceCheckResponse checkMyCoupon(Long couponId, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findById(couponId).orElseThrow(() -> BusinessException.couponNotFound(couponId));

        // null 체크 및 기본값 설정
        BigDecimal discountRate = coupon.getDiscountRate() == null ? BigDecimal.ZERO : coupon.getDiscountRate();
        BigDecimal discountAmount = coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount();
        BigDecimal maxDiscountAmount = coupon.getMaxDiscountAmount() == null ? BigDecimal.ZERO : coupon.getMaxDiscountAmount();
        BigDecimal minPurchaseAmount = coupon.getMinPurchaseAmount() == null ? BigDecimal.ZERO : coupon.getMinPurchaseAmount();

        // 최소 주문 금액 조건 확인
        boolean applicable = orderAmount.compareTo(minPurchaseAmount) >= 0;

        // 실제 할인 금액 계산
        BigDecimal calculatedDiscount = BigDecimal.ZERO;
        if (applicable) {
            // 할인율과 할인금액이 둘 다 있는 경우 할인율을 우선 적용
            if (discountRate.compareTo(BigDecimal.ZERO) > 0) {
                // 퍼센트 할인 적용
                calculatedDiscount = orderAmount.multiply(discountRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                // 최대 할인 금액 제한 적용
                if (maxDiscountAmount.compareTo(BigDecimal.ZERO) > 0 &&
                        calculatedDiscount.compareTo(maxDiscountAmount) > 0) {
                    calculatedDiscount = maxDiscountAmount;
                }
            } else if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                // 정액 할인 적용
                calculatedDiscount = discountAmount;

                // 정액 할인의 경우에도 주문 금액을 초과하지 않도록 제한
                if (calculatedDiscount.compareTo(orderAmount) > 0) {
                    calculatedDiscount = orderAmount;
                }
            }
        }

        // 최종 금액이 음수가 되지 않도록 보장
        BigDecimal finalAmount = orderAmount.subtract(calculatedDiscount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        return SalesPriceCheckResponse.builder()
                .applicable(applicable)
                .discountRate(discountRate)
                .discountAmount(discountAmount)
                .maxDiscountAmount(maxDiscountAmount)
                .minPurchaseAmount(minPurchaseAmount)
                .calculatedDiscountAmount(calculatedDiscount)
                .finalAmount(finalAmount)
                .build();
    }

    /**
     * 사용자 쿠폰 ID로 쿠폰 적용 가능 여부 및 할인 금액 검증
     * @param userCouponId 사용자 쿠폰 ID
     * @param orderAmount 주문 금액 (배송비 포함)
     * @return 쿠폰 적용 결과
     */
    public SalesPriceCheckResponse checkUserCoupon(Long userCouponId, BigDecimal orderAmount) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> BusinessException.couponNotFound(userCouponId));

        // 사용자 쿠폰의 만료 확인
        if (userCoupon.getCoupon().getExpiredAt().isBefore(LocalDateTime.now()) ||
                Boolean.FALSE.equals(userCoupon.getCoupon().getIsActive())) {
            throw BusinessException.couponExpired(userCouponId);
        }

        // 이미 사용된 쿠폰 확인
        if (userCoupon.getUsedAt() != null) {
            throw BusinessException.couponUsed(userCouponId);
        }

        // 실제 쿠폰으로 할인 금액 계산
        return checkMyCoupon(userCoupon.getCoupon().getCouponId(), orderAmount);
    }

    /**
     * 주문 상품에 적용 가능한 쿠폰 목록 조회
     * @param userId 사용자 ID
     * @param productIds 주문 상품 ID 목록
     * @return 적용 가능한 쿠폰 목록
     */
    public List<MyCouponResponse> getApplicableCoupons(Long userId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return getMyCoupons(userId);
        }

        try {
            // 주문 상품 정보 조회
            List<Product> orderProducts = productRepository.findAllById(productIds);
            if (orderProducts.isEmpty()) {
                return List.of(); // 상품이 없으면 빈 목록 반환
            }

            // 상품의 브랜드 ID 수집
            Set<Long> brandIds = orderProducts.stream()
                .map(product -> product.getBrand().getBrandId())
                .collect(Collectors.toSet());

            // 사용자의 모든 유효한 쿠폰 조회
            List<UserCoupon> allUserCoupons = userCouponRepository
                .findByUser_UserIdAndCoupon_ExpiredAtAfterAndCoupon_IsActive(userId, LocalDateTime.now(), true);

            // CouponScope에 따라 필터링
            List<UserCoupon> applicableCoupons = allUserCoupons.stream()
                .filter(userCoupon -> {
                    Coupon coupon = userCoupon.getCoupon();
                    CouponScope scope = coupon.getScope();

                    return switch (scope) {
                        case GLOBAL ->
                            // 전역 쿠폰: 모든 상품에 적용 가능
                                true;
                        case BRAND -> {
                            // 브랜드 쿠폰: 해당 브랜드 상품이 주문에 포함되어 있는지 확인
                            if (coupon.getBrand() != null) {
                                yield brandIds.contains(coupon.getBrand().getBrandId());
                            }
                            yield false;
                        }
                        case PRODUCT -> {
                            // 상품 쿠폰: 해당 상품이 주문에 포함되어 있는지 확인
                            if (coupon.getProduct() != null) {
                                yield productIds.contains(coupon.getProduct().getProductId());
                            }
                            yield false;
                        }
                        default -> false;
                    };
                })
                .toList();

            // MyCouponResponse로 변환하여 반환
            return applicableCoupons.stream()
                .map(MyCouponResponse::from)
                .collect(Collectors.toList());

        } catch (Exception e) {
            // 오류 발생 시 전체 쿠폰 목록 반환
            return getMyCoupons(userId);
        }
    }

}