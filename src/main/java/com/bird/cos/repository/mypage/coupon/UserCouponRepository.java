package com.bird.cos.repository.mypage.coupon;

import com.bird.cos.domain.coupon.UserCoupon;
import com.bird.cos.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    boolean existsByUser_UserIdAndCoupon_CouponId(Long userId, Long couponId);

    Page<UserCoupon> findByUser_UserId(Long userId, Pageable pageable);

    List<UserCoupon> findByUser_UserIdAndCoupon_ExpiredAtAfterAndCoupon_IsActive(
            Long userId, LocalDateTime now, boolean isActive);


    @EntityGraph(attributePaths = {
            "coupon",
            "coupon.product",
            "coupon.product.brand",
            "couponStatus"
    })
    List<UserCoupon> findByUserAndOrderIsNull(User user);

    long countByUser_UserIdAndCoupon_IsActive(Long userId, boolean isActive);
}
