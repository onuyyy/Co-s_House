package com.bird.cos.config;

import com.bird.cos.security.ProblemDetailsAccessDeniedHandler;
import com.bird.cos.security.ProblemDetailsAuthenticationEntryPoint;
import com.bird.cos.security.RegisterSecurityFilter;
import com.bird.cos.security.oauth.SocialOAuth2UserService;
import com.bird.cos.security.oauth.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
/**
 * Spring Security 핵심 설정
 * - 정적 리소스/공개 페이지 퍼밋, 관리자 경로 권한(admin_role)
 * - 세션 기반 SecurityContext 저장, 동시 세션 1회
 * - dev 프로파일에서만 HTTP Basic 허용
 * - 인증/인가 예외 JSON 처리(ProblemDetails*)
 */
// 메서드 보안 애너테이션(@PreAuthorize, @Secured, @RolesAllowed) 활성화
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final Environment environment;
    private final SocialOAuth2UserService socialOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // AuthenticationManager를 빈으로 노출(필요 시 주입받아 사용)
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        // 세션 기반 SecurityContext 저장 전략 사용
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public ProblemDetailsAuthenticationEntryPoint problemDetailsAuthenticationEntryPoint() {
        // 인증 실패(401) 시 JSON 형태로 에러 응답 생성
        return new ProblemDetailsAuthenticationEntryPoint();
    }

    @Bean
    public ProblemDetailsAccessDeniedHandler problemDetailsAccessDeniedHandler() {
        // 인가 거부(403) 시 JSON 형태로 에러 응답 생성
        return new ProblemDetailsAccessDeniedHandler();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
                // 서버 렌더/동일 오리진 기준이므로 CSRF/CORS는 기본 비활성화(필요 시 별도 구성)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 0) 이메일 인증 허용
                        .requestMatchers("/auth/email/**").permitAll()

                        // 0-1) OAuth2 인증 엔드포인트 전용 허용
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // 1) 정적 리소스 전부 허용
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(
                                "/favicon.ico",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/images/uploaded/**"
                        ).permitAll()

                        // 2) 루트 및 공개 페이지(GET)
                        .requestMatchers(HttpMethod.GET,
                                "/",
                                "/account/register",
                                "/account/reset",
                                "/controller/register/login",
                                "/cart", //todo: 장바구니 권한
                                "/product/**", //todo: 상품 권한
                                "/product", //todo: 상품 권한
                                "/community",
                                "/events/**",
                                "/events/**", //todo: 이벤트 권한
                                "/brands/**",
                                "/notices/**",
                                "/posts",
                                "/posts/**", //todo: 커뮤니티 권한
                                "/error" //todo: 에러
                        ).permitAll()
                        .requestMatchers("/error").permitAll()

                        // 3) 회원가입/로그인/로그아웃 공개 API(POST)
                        .requestMatchers(HttpMethod.POST,
                                "/controller/register/register",
                                "/controller/register/login",
                                "/controller/register/login-form",
                                "/controller/register/logout"
                        ).permitAll()

                        // 4) 관리자 영역은 관리자 권한 필요(권한명: admin_role)
                        .requestMatchers("/api/admin/**").hasAuthority("admin_role")

                        // 5) 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(problemDetailsAuthenticationEntryPoint())
                        .accessDeniedHandler(problemDetailsAccessDeniedHandler())
                )
                // 기본 폼 로그인은 비활성화(커스텀 로그인 사용). dev 프로파일일 때만 Basic 허용
                .oauth2Login(oauth -> oauth
                        .loginPage("/controller/register/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(socialOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .formLogin(AbstractHttpConfigurer::disable);

        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            // 개발 편의를 위한 HTTP Basic (운영에서는 비활성)
            http.httpBasic(org.springframework.security.config.Customizer.withDefaults());
        } else {
            http.httpBasic(AbstractHttpConfigurer::disable);
        }

        http
                .sessionManagement(session -> session
                        // 세션 기반: 필요 시 생성, 세션 고정 보호(newSession)
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().newSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                // SecurityContext를 세션에 저장/조회
                .securityContext(sc -> sc.securityContextRepository(securityContextRepository))
                .requestCache(c -> c.requestCache(new NullRequestCache()))
                // 회원가입 보강 보안 필터 추가(Origin/Referer/RateLimit/JSON 강제)
                .addFilterBefore(registerSecurityFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public RegisterSecurityFilter registerSecurityFilter() {
        // 회원가입 엔드포인트 POST 요청에만 동작하는 경량 필터
        return new RegisterSecurityFilter();
    }
}
