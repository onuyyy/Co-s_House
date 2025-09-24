package com.bird.cos.domain.order;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "`ORDER`")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_status", referencedColumnName = "code_id", nullable = false)
    private CommonCode orderStatusCode;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "order_date", insertable = false, updatable = false)
    private LocalDateTime orderDate;

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;

    @Column(name = "order_created_at", insertable = false, updatable = false)
    private LocalDateTime orderCreatedAt;

    @Column(name = "order_updated_at", insertable = false, updatable = false)
    private LocalDateTime orderUpdatedAt;

}
