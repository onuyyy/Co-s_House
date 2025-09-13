package com.bird.cos.domain.user;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.order.Order;
import com.bird.cos.domain.proudct.Coupon;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_COUPON")
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")
    private Long userCouponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_status", referencedColumnName = "code_id")
    private CommonCode couponStatus;

    @Column(name = "issued_at", insertable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

}