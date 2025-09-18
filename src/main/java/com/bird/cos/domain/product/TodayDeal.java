package com.bird.cos.domain.product;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TODAY_DEAL")
public class TodayDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deal_id")
    private Long dealId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "deal_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal dealPrice;

    @Column(name = "original_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "discount_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal discountRate;

    @Column(name = "limited_quantity")
    private Integer limitedQuantity;

    @Column(name = "sold_quantity")
    private Integer soldQuantity = 0;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    @Column(name = "todaydeal_is_active")
    private Boolean todaydealIsActive = true;

    @Column(name = "todaydeal_created_at", insertable = false, updatable = false)
    private LocalDateTime todaydealCreatedAt;

}