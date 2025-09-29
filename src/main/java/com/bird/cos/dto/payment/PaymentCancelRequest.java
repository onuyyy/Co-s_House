package com.bird.cos.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentCancelRequest {

    @NotBlank
    private String paymentKey;

    private String cancelReason = "사용자 요청";

    @Min(1)
    private Long cancelAmount;
}
