package com.bird.cos.domain.payment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "TossPayment")
@Table(name = "COS_PAYMENT")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "order_id", nullable = false, length = 100, unique = true)
    private String orderId;

    @Column(name = "order_name", length = 200)
    private String orderName;

    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    @Column(name = "customer_name", length = 50)
    private String customerName;

    @Column(name = "payment_key", length = 255, unique = true)
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private PaymentStatus status;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "failure_message", length = 255)
    private String failureMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onPersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markApproved(String paymentKey, LocalDateTime approvedAt) {
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.APPROVED;
        this.approvedAt = approvedAt;
        this.failureCode = null;
        this.failureMessage = null;
    }

    public void markFailed(String failureCode, String failureMessage) {
        this.status = PaymentStatus.FAILED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
    }

    public void markCancelled(String failureCode, String failureMessage) {
        this.status = PaymentStatus.CANCELLED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
    }

    public void updatePendingInfo(String orderName, String customerEmail, String customerName, LocalDateTime requestedAt) {
        this.orderName = orderName;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.requestedAt = requestedAt;
        this.status = PaymentStatus.PENDING;
        this.failureCode = null;
        this.failureMessage = null;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
