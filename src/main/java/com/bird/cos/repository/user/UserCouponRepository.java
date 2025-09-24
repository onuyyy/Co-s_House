package com.bird.cos.repository.user;

import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserCoupon;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    @EntityGraph(attributePaths = {
            "coupon",
            "coupon.product",
            "coupon.product.brand",
            "couponStatus"
    })
    List<UserCoupon> findByUserAndOrderIsNull(User user);
}

