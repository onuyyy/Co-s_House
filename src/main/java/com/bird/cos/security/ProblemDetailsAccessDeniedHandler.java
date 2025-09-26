package com.bird.cos.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

/**
 * 접근 거부 처리 핸들러(403)
 * - RFC7807 ProblemDetail 형식으로 응답
 * - traceId는 MDC("traceId") 또는 요청 헤더("X-Trace-Id")에서 추출
 * - type URI는 설정값(cos.problem.base-uri) 기반으로 생성
 */
public class ProblemDetailsAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cos.problem.base-uri:}")
    private String problemBaseUri;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException, ServletException {
        // Accept 헤더를 확인하여 JSON 요청인지 HTML 요청인지 판단
        String acceptHeader = request.getHeader("Accept");
        String userAgent = request.getHeader("User-Agent");

        // 웹 브라우저 요청 판단: Accept 헤더에 text/html이 포함되어 있는 경우
        boolean isBrowserRequest = acceptHeader != null && acceptHeader.contains("text/html");

        // JSON 요청 판단
        boolean isJsonRequest = acceptHeader != null &&
            (acceptHeader.contains("application/json") || acceptHeader.contains("application/problem+json"));

        // 명시적으로 JSON을 요구하는 경우에만 JSON 응답
        // 관리자 페이지(/api/admin)는 브라우저 접근 시 HTML 에러 페이지를 표시해야 함
        if (isJsonRequest || (request.getRequestURI().startsWith("/api/") && !isBrowserRequest && !request.getRequestURI().startsWith("/api/admin/"))) {
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
            pd.setTitle("접근 거부");
            pd.setDetail(ex != null && ex.getMessage() != null ? ex.getMessage() : "권한이 부족합니다.");
            pd.setType(buildType("forbidden"));
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

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), pd);
        } else {
            // HTML 요청인 경우 403 에러 페이지로 포워딩
            request.getRequestDispatcher("/error/403").forward(request, response);
        }
    }

    private URI buildType(String slug) {
        String base = problemBaseUri;
        if (base != null && !base.isBlank()) {
            if (!base.endsWith("/")) base = base + "/";
            try {
                return URI.create(base + slug);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return URI.create("urn:problem-type:" + slug);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
