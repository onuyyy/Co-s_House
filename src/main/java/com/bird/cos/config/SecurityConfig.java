package com.bird.cos.config;

import com.bird.cos.security.RegisterSecurityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 테스트 HTML과 JSON POST가 막히지 않도록 CSRF 비활성화
                .csrf(csrf -> csrf.disable())
                // URL 접근 제어
                .authorizeHttpRequests(auth -> auth
                        // 정적/테스트 페이지 허용 + 회원가입 페이지(GET)
                        .requestMatchers(HttpMethod.GET,
                                "/",
                                "/account/register",
                                "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // 회원가입/로그인/로그아웃은 모두 허용
                        .requestMatchers("/controller/register/**").permitAll()
                        // 관리자 영역은 ADMIN 권한 필요 (custom authority 명칭 사용)
                        .requestMatchers("/api/admin/**").hasAuthority("admin_role")
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 기본 로그인/HTTP Basic 비활성화 (커스텀 엔드포인트 사용)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 세션 기반
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // 회원가입 보강 보안 필터 추가
                .addFilterBefore(registerSecurityFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public RegisterSecurityFilter registerSecurityFilter() {
        return new RegisterSecurityFilter();
    }
}
