package com.bird.cos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 회원가입 엔드포인트(/controller/register/register)에 한정하여 적용되는 경량 보안 필터.
 * JSON Content-Type 강제
 * 동일 오리진(Origin/Referer) 확인
 * IP 기준 레이트 리밋 (기본: 60초에 5회)
 */
public class RegisterSecurityFilter extends OncePerRequestFilter {

    private static final String TARGET_PATH = "/controller/register/register";

    private static final int LIMIT_WINDOW_SECONDS = 60;
    private static final int LIMIT_MAX_REQUESTS = 60;

    private final Map<String, Deque<Long>> ipBuckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 컨텍스트 패스 영향을 피하기 위해 servletPath 기준으로 비교
        String servletPath = request.getServletPath();
        return !"POST".equalsIgnoreCase(request.getMethod()) || !TARGET_PATH.equals(servletPath);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1) Content-Type 확인 (JSON만 허용)
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            writeError(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type must be application/json", request);
            return;
        }

        // 2) 동일 Origin/Referer 확인 (존재할 때만 검사)
        String expectedOrigin = buildOrigin(request);
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        if (StringUtils.hasText(origin) && !origin.equalsIgnoreCase(expectedOrigin)) {
            writeError(response, HttpStatus.FORBIDDEN, "invalid origin", request);
            return;
        }
        if (!StringUtils.hasText(origin) && StringUtils.hasText(referer) && !referer.startsWith(expectedOrigin)) {
            writeError(response, HttpStatus.FORBIDDEN, "invalid referer", request);
            return;
        }

        // 3) 간단한 IP 기반 레이트 리밋
        String key = clientIp(request);
        long now = Instant.now().toEpochMilli();
        Deque<Long> q = ipBuckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q) {
            long cutoff = now - (LIMIT_WINDOW_SECONDS * 1000L);
            while (!q.isEmpty() && q.peekFirst() < cutoff) {
                q.pollFirst();
            }
            if (q.size() >= LIMIT_MAX_REQUESTS) {
                writeError(response, HttpStatus.TOO_MANY_REQUESTS, "too many requests", request);
                return;
            }
            q.addLast(now);
            // 큐가 비워졌다면 버킷 제거하여 메모리 누수 완화
            if (q.isEmpty()) {
                ipBuckets.remove(key);
            }
        }

        filterChain.doFilter(request, response);
    }

    private static String buildOrigin(HttpServletRequest request) {
        int port = request.getServerPort();
        String scheme = request.getScheme();
        String host = request.getServerName();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + host + (defaultPort ? "" : ":" + port);
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            int idx = xff.indexOf(',');
            return idx > 0 ? xff.substring(0, idx).trim() : xff.trim();
        }
        String xri = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xri)) return xri.trim();
        return request.getRemoteAddr();
    }

    private static void writeError(HttpServletResponse response, HttpStatus status, String message, HttpServletRequest req)
            throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        String body = "{" +
                "\"timestamp\":\"" + Instant.now() + "\"," +
                "\"status\":" + status.value() + "," +
                "\"error\":\"" + escape(status.getReasonPhrase()) + "\"," +
                "\"message\":\"" + escape(message) + "\"," +
                "\"path\":\"" + escape(req.getRequestURI()) + "\"" +
                "}";
        response.getWriter().write(body);
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
