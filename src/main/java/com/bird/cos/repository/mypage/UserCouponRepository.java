package com.bird.cos.repository.mypage;

import com.bird.cos.domain.coupon.UserCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    boolean existsByUser_UserIdAndCoupon_CouponId(Long userId, Long couponId);

    Page<UserCoupon> findByUser_UserId(Long userId, Pageable pageable);
}
