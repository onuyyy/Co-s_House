package com.bird.cos.domain.product;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "COUPON")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "coupon_title", length = 255, nullable = false)
    private String couponTitle;

    @Column(name = "coupon_description", columnDefinition = "TEXT")
    private String couponDescription;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_purchase_amount", precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount;

    @Column(name = "coupon_image_url", length = 500)
    private String couponImageUrl;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "coupon_created_at", insertable = false, updatable = false)
    private LocalDateTime couponCreatedAt;

    @Column(name = "coupon_updated_at", insertable = false, updatable = false)
    private LocalDateTime couponUpdatedAt;

}