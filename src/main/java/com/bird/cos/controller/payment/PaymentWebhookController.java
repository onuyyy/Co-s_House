package com.bird.cos.controller.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Slf4j
public class PaymentWebhookController {

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> payload,
                                              @RequestHeader(value = "TossPayments-Signature", required = false) String signature) {
        log.info("[TossWebhook] signature={}, payload={} ", signature, payload);
        return ResponseEntity.ok().build();
    }
}

