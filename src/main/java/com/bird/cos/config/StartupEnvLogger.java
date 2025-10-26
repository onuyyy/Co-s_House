package com.bird.cos.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class StartupEnvLogger implements ApplicationRunner {

    private final Environment environment;

    public StartupEnvLogger(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        logEnv("KAKAO_CLIENT_ID", "spring.security.oauth2.client.registration.kakao.client-id");
        logEnv("KAKAO_CLIENT_SECRET", "spring.security.oauth2.client.registration.kakao.client-secret");
        logEnv("NAVER_CLIENT_ID", "spring.security.oauth2.client.registration.naver.client-id");
        logEnv("NAVER_CLIENT_SECRET", "spring.security.oauth2.client.registration.naver.client-secret");
        logEnv("TOSS_CLIENT_KEY", "toss.payments.client-key");
        logEnv("TOSS_SECRET_KEY", "toss.payments.secret-key");
    }

    private void logEnv(String envKey, String propertyKey) {
        String envValue = mask(System.getenv(envKey));
        String propertyValue = mask(environment.getProperty(propertyKey));
        log.info("[EnvCheck] {} env={} property={}", envKey, envValue, propertyValue);
    }

    private String mask(String value) {
        return Optional.ofNullable(value)
                .map(v -> v.length() <= 4 ? "****" : v.substring(0, 2) + "***" + v.substring(v.length() - 2))
                .orElse("<not set>");
    }
}
