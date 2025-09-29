package com.bird.cos.controller.payment;

import com.bird.cos.dto.payment.*;
import com.bird.cos.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentApiController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    public ResponseEntity<PaymentIntentResponse> createIntent(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirm(@Valid @RequestBody TossConfirmRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(request));
    }

    @PostMapping("/cancel")
    public ResponseEntity<PaymentResponse> cancel(@Valid @RequestBody PaymentCancelRequest request) {
        return ResponseEntity.ok(paymentService.cancelPayment(request));
    }
}

