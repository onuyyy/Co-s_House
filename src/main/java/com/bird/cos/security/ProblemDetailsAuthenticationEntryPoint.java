package com.bird.cos.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

/**
 * 인증 실패 진입점(401)
 * - RFC7807 ProblemDetail 형식으로 응답
 * - traceId는 MDC("traceId") 또는 요청 헤더("X-Trace-Id")에서 추출
 * - type URI는 설정값(cos.problem.base-uri) 기반으로 생성
 */
public class ProblemDetailsAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cos.problem.base-uri:}")
    private String problemBaseUri;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("인증 필요");
        pd.setDetail(authException != null && authException.getMessage() != null ? authException.getMessage() : "로그인이 필요합니다.");
        pd.setType(buildType("unauthorized"));
        try {
            pd.setInstance(URI.create(request.getRequestURI()));
        } catch (IllegalArgumentException ignored) {
        }

        String traceId = firstNonBlank(MDC.get("traceId"), request.getHeader("X-Trace-Id"));
        if (traceId != null && !traceId.isBlank()) {
            pd.setProperty("traceId", traceId);
        }
        pd.setProperty("path", request.getRequestURI());
        pd.setProperty("method", request.getMethod());
        pd.setProperty("timestamp", Instant.now().toString());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), pd);
    }

    private java.net.URI buildType(String slug) {
        String base = problemBaseUri;
        if (base != null && !base.isBlank()) {
            if (!base.endsWith("/")) base = base + "/";
            try {
                return java.net.URI.create(base + slug);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return java.net.URI.create("urn:problem-type:" + slug);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
