package com.bird.cos.repository.mypage.coupon;

import com.bird.cos.domain.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {

    java.util.List<Coupon> findByBrand_BrandIdAndIsActiveTrueOrderByExpiredAtAsc(Long brandId);
}
