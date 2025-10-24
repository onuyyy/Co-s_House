package com.bird.cos.service.user;

import com.bird.cos.config.WebMvcConfig;
import com.bird.cos.domain.user.Point;
import com.bird.cos.domain.user.User;
import com.bird.cos.repository.user.PointRepository;
import com.bird.cos.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@EntityScan(basePackages = "com.bird.cos.domain")
@EnableJpaRepositories(basePackages = "com.bird.cos.repository")
@ActiveProfiles("test")
@ImportAutoConfiguration(exclude = {WebMvcConfig.class})
@SpringBootTest(classes = com.bird.cos.CosApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private OAuth2AuthorizedClientService authorizedClientService;

    @Test
    @DisplayName("포인트가 정상적으로 차감된다")
    void givenSufficientPoints_whenUsePoints_thenPointsReduced() {
        Long userId = createUserWithPoints(10_000);

        pointService.usePoints(userId, 3_000, "테스트 차감", "REF001", "TEST");

        Integer remaining = pointService.getAvailablePoints(userId);
        assertThat(remaining).isEqualTo(7_000);
    }

    @Test
    @DisplayName("동시에 포인트를 차감하면 race condition이 실제로 발생한다")
    void givenConcurrentRequests_whenUsePoints_thenRealRaceConditionOccurs() throws InterruptedException {
        Long userId = createUserWithPoints(10_000);

        int threadCount = 10;
        // 스레드 풀 생성
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 모든 스레드가 동시에 시작하도록 'startLatch' 추가
        // CountDownLatch는 모든 스레드가 완료되기를 기다림
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger errorCount = new AtomicInteger();

        // 각 스레드에서 메서드 실행
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // 시작 신호 대기
                    pointService.usePoints(userId, 2_000,
                            "동시성 테스트",
                            "REF" + Thread.currentThread().getId(),
                            "TEST");
                } catch (Exception ex) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 모든 스레드를 동시에 출발시킴
        startLatch.countDown();

        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        Integer remaining = pointService.getAvailablePoints(userId);
        System.out.printf("동시성 테스트 최종 잔액: %d, 실패 횟수: %d%n", remaining, errorCount.get());

        assertThat(remaining).isEqualTo(0);
    }

    private Long createUserWithPoints(int initialPoint) {
        User user = userRepository.save(User.builder()
                .userName("test-user")
                .userNickname("test-nick")
                .userEmail("tester@example.com")
                .emailVerified(true)
                .termsAgreed(true)
                .build());

        Point point = Point.builder()
                .pointAmount(initialPoint)
                .user(user)
                .build();
        pointRepository.save(point);

        return user.getUserId();
    }
}
