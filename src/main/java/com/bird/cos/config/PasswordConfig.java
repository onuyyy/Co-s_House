package com.bird.cos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    /**
     * 비밀번호 인코더 빈 설정
     * BCrypt 알고리즘 사용(적응형 해시)으로 안전한 비밀번호 저장을 보장합니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
