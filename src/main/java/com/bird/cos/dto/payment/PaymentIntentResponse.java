package com.bird.cos.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentIntentResponse {

    private final String orderId;
    private final Long amount;
    private final String orderName;
    private final String customerEmail;
    private final String customerName;
    private final String clientKey;
    private final String successUrl;
    private final String failUrl;
}

