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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private RestTemplateBuilder restTemplateBuilder;
    private RestTemplate restTemplate;
    private TossPaymentsProperties.Property tossProperties;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        restTemplateBuilder = mock(RestTemplateBuilder.class);
        restTemplate = mock(RestTemplate.class);
        tossProperties = new TossPaymentsProperties.Property(
                "test-secret",
                "test-client",
                "https://success",
                "https://fail"
        );
        paymentService = new PaymentService(paymentRepository, tossProperties, restTemplateBuilder);
        ReflectionTestUtils.setField(paymentService, "restTemplate", restTemplate);
    }

    // 신규 주문에 대한 결제 인텐트를 생성하고 저장된 값이 응답에 반영되는지 확인
    @Test
    void createPaymentIntent_WhenPaymentNotExists_CreatesPendingPayment() {
        PaymentRequest request = PaymentRequest.builder()
                .orderId("order-1")
                .amount(15000L)
                .orderName("테스트 주문")
                .customerEmail("user@example.com")
                .customerName("홍길동")
                .build();

        when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentIntentResponse response = paymentService.createPaymentIntent(request);

        assertThat(response.getOrderId()).isEqualTo("order-1");
        assertThat(response.getAmount()).isEqualTo(15000L);
        assertThat(response.getOrderName()).isEqualTo("테스트 주문");
        assertThat(response.getCustomerEmail()).isEqualTo("user@example.com");
        assertThat(response.getCustomerName()).isEqualTo("홍길동");
        assertThat(response.getClientKey()).isEqualTo("test-client");
        assertThat(response.getSuccessUrl()).isEqualTo("https://success");
        assertThat(response.getFailUrl()).isEqualTo("https://fail");

        verify(paymentRepository).save(any(Payment.class));
    }

    // 이미 승인된 주문으로 결제 인텐트를 생성하면 예외가 발생하는지 확인
    @Test
    void createPaymentIntent_WhenPaymentAlreadyApproved_ThrowsException() {
        Payment approved = Payment.builder()
                .orderId("order-2")
                .amount(BigDecimal.valueOf(10000))
                .status(PaymentStatus.APPROVED)
                .build();
        PaymentRequest request = PaymentRequest.builder()
                .orderId("order-2")
                .amount(10000L)
                .build();

        when(paymentRepository.findByOrderId("order-2")).thenReturn(Optional.of(approved));

        assertThatThrownBy(() -> paymentService.createPaymentIntent(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 승인된 주문입니다.");

        verify(paymentRepository, never()).save(any());
    }

    // 결제 승인 시 토스 응답이 정상일 때 Payment 데이터가 승인 상태로 갱신되는지 확인
    @Test
    void confirmPayment_WhenResponseSuccess_MarksApprovedAndReturnsResult() {
        Payment payment = Payment.builder()
                .orderId("order-3")
                .amount(BigDecimal.valueOf(20000))
                .status(PaymentStatus.PENDING)
                .build();
        TossConfirmRequest request = new TossConfirmRequest();
        ReflectionTestUtils.setField(request, "paymentKey", "pay-key");
        ReflectionTestUtils.setField(request, "orderId", "order-3");
        ReflectionTestUtils.setField(request, "amount", 20000L);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("paymentKey", "pay-key-response");
        responseBody.put("approvedAt", OffsetDateTime.now().toString());

        when(paymentRepository.findByOrderId("order-3")).thenReturn(Optional.of(payment));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.confirmPayment(request);

        assertThat(response.getOrderId()).isEqualTo("order-3");
        assertThat(response.getPaymentKey()).isEqualTo("pay-key-response");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(response.getAmount()).isEqualTo(20000L);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getPaymentKey()).isEqualTo("pay-key-response");
        assertThat(payment.getApprovedAt()).isNotNull();

        verify(paymentRepository).save(payment);
    }

    // 결제 승인 요청 시 금액이 일치하지 않으면 예외가 발생하고 외부 호출이 차단되는지 확인
    @Test
    void confirmPayment_WhenAmountMismatch_ThrowsException() {
        Payment payment = Payment.builder()
                .orderId("order-4")
                .amount(BigDecimal.valueOf(10000))
                .status(PaymentStatus.PENDING)
                .build();
        TossConfirmRequest request = new TossConfirmRequest();
        ReflectionTestUtils.setField(request, "paymentKey", "key");
        ReflectionTestUtils.setField(request, "orderId", "order-4");
        ReflectionTestUtils.setField(request, "amount", 20000L);

        when(paymentRepository.findByOrderId("order-4")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.confirmPayment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 금액이 일치하지 않습니다.");

        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(Map.class));
    }

    // 토스 승인 API 호출이 실패했을 때 결제 상태가 FAILED로 변경되는지 확인
    @Test
    void confirmPayment_WhenTossReturnsError_MarksFailedAndThrows() {
        Payment payment = Payment.builder()
                .orderId("order-5")
                .amount(BigDecimal.valueOf(30000))
                .status(PaymentStatus.PENDING)
                .build();
        TossConfirmRequest request = new TossConfirmRequest();
        ReflectionTestUtils.setField(request, "paymentKey", "key");
        ReflectionTestUtils.setField(request, "orderId", "order-5");
        ReflectionTestUtils.setField(request, "amount", 30000L);

        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                "error".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(paymentRepository.findByOrderId("order-5")).thenReturn(Optional.of(payment));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(exception);

        assertThatThrownBy(() -> paymentService.confirmPayment(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 승인에 실패했습니다");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(paymentRepository).save(payment);
    }

    // 결제 취소 요청이 성공하면 상태가 CANCELLED로 변경되는지 확인
    @Test
    void cancelPayment_WhenTossCancelsSuccessfully_MarksCancelled() {
        Payment payment = Payment.builder()
                .orderId("order-6")
                .paymentKey("payment-key")
                .amount(BigDecimal.valueOf(40000))
                .status(PaymentStatus.APPROVED)
                .build();
        PaymentCancelRequest request = new PaymentCancelRequest();
        ReflectionTestUtils.setField(request, "paymentKey", "payment-key");
        ReflectionTestUtils.setField(request, "cancelReason", "사용자 요청");
        ReflectionTestUtils.setField(request, "cancelAmount", 10000L);

        when(paymentRepository.findByPaymentKey("payment-key")).thenReturn(Optional.of(payment));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("status", "CANCELLED")));
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.cancelPayment(request);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(response.getOrderId()).isEqualTo("order-6");
        assertThat(tuple(response.getPaymentKey(), response.getAmount()))
                .isEqualTo(tuple("payment-key", 40000L));

        verify(paymentRepository).save(payment);
    }

    // 결제 취소 중 토스에서 오류가 발생할 경우 예외를 전달하는지 확인
    @Test
    void cancelPayment_WhenTossReturnsError_ThrowsException() {
        Payment payment = Payment.builder()
                .orderId("order-7")
                .paymentKey("payment-key")
                .amount(BigDecimal.valueOf(50000))
                .status(PaymentStatus.APPROVED)
                .build();
        PaymentCancelRequest request = new PaymentCancelRequest();
        ReflectionTestUtils.setField(request, "paymentKey", "payment-key");

        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                "error".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(paymentRepository.findByPaymentKey("payment-key")).thenReturn(Optional.of(payment));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(exception);

        assertThatThrownBy(() -> paymentService.cancelPayment(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 취소에 실패했습니다");

        verify(paymentRepository, never()).save(any());
    }
}
