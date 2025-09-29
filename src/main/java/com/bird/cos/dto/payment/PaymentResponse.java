package com.bird.cos.dto.payment;

import com.bird.cos.domain.payment.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {

    private final String orderId;
    private final String paymentKey;
    private final PaymentStatus status;
    private final Long amount;
    private final LocalDateTime approvedAt;
}
