package com.bird.cos.domain.order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "REFUND")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long refundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment paymentId;

    @Column(name = "refund_type", length = 10, nullable = false)
    private String refundType;

    @Column(name = "refund_reason", length = 255)
    private String refundReason;

    @Column(name = "refund_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal refundAmount;

    @Column(name = "refund_status", length = 20)
    private String refundStatus = "REQUESTED";

    @Column(name = "requested_at", nullable = false)
    private LocalDate requestedAt;

    @Column(name = "completed_at")
    private LocalDate completedAt;

}
