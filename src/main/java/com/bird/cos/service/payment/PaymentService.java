package com.bird.cos.service.payment;

import com.bird.cos.config.TossPaymentsProperties;
import com.bird.cos.domain.payment.Payment;
import com.bird.cos.domain.payment.PaymentStatus;
import com.bird.cos.dto.payment.PaymentCancelRequest;
import com.bird.cos.dto.payment.PaymentIntentResponse;
import com.bird.cos.dto.payment.PaymentRequest;
import com.bird.cos.dto.payment.PaymentResponse;
import com.bird.cos.dto.payment.TossConfirmRequest;
import com.bird.cos.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final String TOSS_CONFIRM_ENDPOINT = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String TOSS_CANCEL_ENDPOINT = "https://api.tosspayments.com/v1/payments/{paymentKey}/cancel";

    private final PaymentRepository paymentRepository;
    private final TossPaymentsProperties.Property tossProperties;
    private final RestTemplateBuilder restTemplateBuilder;

    private RestTemplate restTemplate;

    private RestTemplate restTemplate() {
        if (restTemplate == null) {
            restTemplate = restTemplateBuilder
                    .setConnectTimeout(Duration.ofSeconds(5))
                    .setReadTimeout(Duration.ofSeconds(10))
                    .build();
        }
        return restTemplate;
    }

    @Transactional
    public PaymentIntentResponse createPaymentIntent(PaymentRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseGet(() -> Payment.builder()
                        .orderId(request.getOrderId())
                        .amount(BigDecimal.valueOf(request.getAmount()))
                        .status(PaymentStatus.PENDING)
                        .build());

        if (payment.getStatus() == PaymentStatus.APPROVED) {
            throw new IllegalStateException("이미 승인된 주문입니다.");
        }

        payment.updatePendingInfo(request.getOrderName(), request.getCustomerEmail(), request.getCustomerName(), LocalDateTime.now());
        payment.setAmount(BigDecimal.valueOf(request.getAmount()));

        Payment saved = paymentRepository.save(payment);

        return PaymentIntentResponse.builder()
                .orderId(saved.getOrderId())
                .amount(saved.getAmount().longValue())
                .orderName(saved.getOrderName())
                .customerEmail(saved.getCustomerEmail())
                .customerName(saved.getCustomerName())
                .clientKey(tossProperties.clientKey())
                .successUrl(tossProperties.successUrl())
                .failUrl(tossProperties.failUrl())
                .build();
    }

    @Transactional
    public PaymentResponse confirmPayment(TossConfirmRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        if (payment.getAmount().compareTo(BigDecimal.valueOf(request.getAmount())) != 0) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentKey", request.getPaymentKey());
        payload.put("orderId", request.getOrderId());
        payload.put("amount", request.getAmount());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(tossProperties.secretKey(), "");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate().postForEntity(TOSS_CONFIRM_ENDPOINT, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new IllegalStateException("토스 결제 승인 응답이 비어 있습니다.");
            }

            String paymentKey = asString(body.get("paymentKey"));
            if (paymentKey == null || paymentKey.isBlank()) {
                paymentKey = request.getPaymentKey();
            }
            LocalDateTime approvedAt = parseDate(asString(body.get("approvedAt")));

            payment.markApproved(paymentKey, approvedAt);
            paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .orderId(payment.getOrderId())
                    .paymentKey(payment.getPaymentKey())
                    .status(payment.getStatus())
                    .amount(payment.getAmount().longValue())
                    .approvedAt(payment.getApprovedAt())
                    .build();

        } catch (HttpStatusCodeException ex) {
            log.error("토스 결제 승인 호출 실패: {}", ex.getResponseBodyAsString(), ex);
            payment.markFailed(String.valueOf(ex.getStatusCode().value()), ex.getMessage());
            paymentRepository.save(payment);
            throw new IllegalStateException("결제 승인에 실패했습니다: " + ex.getMessage());
        }
    }

    @Transactional
    public PaymentResponse cancelPayment(PaymentCancelRequest request) {
        Payment payment = paymentRepository.findByPaymentKey(request.getPaymentKey())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        long cancelAmount = request.getCancelAmount() != null ? request.getCancelAmount() : payment.getAmount().longValue();

        Map<String, Object> payload = new HashMap<>();
        payload.put("cancelReason", request.getCancelReason());
        payload.put("cancelAmount", cancelAmount);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(tossProperties.secretKey(), "");

        String url = UriComponentsBuilder.fromHttpUrl(TOSS_CANCEL_ENDPOINT)
                .buildAndExpand(payment.getPaymentKey())
                .toUriString();

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate().postForEntity(url, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new IllegalStateException("토스 결제 취소 응답이 비어 있습니다.");
            }

            payment.markCancelled("CANCEL", request.getCancelReason());
            paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .orderId(payment.getOrderId())
                    .paymentKey(payment.getPaymentKey())
                    .status(payment.getStatus())
                    .amount(payment.getAmount().longValue())
                    .approvedAt(payment.getApprovedAt())
                    .build();
        } catch (HttpStatusCodeException ex) {
            log.error("토스 결제 취소 호출 실패: {}", ex.getResponseBodyAsString(), ex);
            throw new IllegalStateException("결제 취소에 실패했습니다: " + ex.getMessage());
        }
    }

    private LocalDateTime parseDate(String source) {
        if (source == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(source).toLocalDateTime();
        } catch (DateTimeParseException ex) {
            log.warn("날짜 파싱 실패: {}", source, ex);
            return null;
        }
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String str) {
            return str;
        }
        return value.toString();
    }
}
