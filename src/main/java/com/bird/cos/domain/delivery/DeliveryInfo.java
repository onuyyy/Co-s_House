package com.bird.cos.domain.delivery;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.proudct.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "DELIVERY_INFO")
public class DeliveryInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_info_id")
    private Long deliveryInfoId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", unique = true, nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_type", referencedColumnName = "code_id", nullable = false)
    private CommonCode deliveryTypeCode;

    @Column(name = "delivery_fee", precision = 8, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "free_shipping_threshold", precision = 10, scale = 2)
    private BigDecimal freeShippingThreshold;

    @Column(name = "expected_delivery_days")
    private Integer expectedDeliveryDays = 3;

    @Column(name = "is_today_departure")
    private Boolean isTodayDeparture = false;

    @Column(name = "delivery_company", length = 50)
    private String deliveryCompany;

    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

    @Column(name = "delivery_created_at", insertable = false, updatable = false)
    private LocalDateTime deliveryCreatedAt;

    @Column(name = "delivery_updated_at", insertable = false, updatable = false)
    private LocalDateTime deliveryUpdatedAt;

}