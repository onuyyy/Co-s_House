package com.bird.cos.security;

import com.bird.cos.aop.LoggingService;
import com.bird.cos.aop.UserActivityRequest;
import com.bird.cos.domain.log.UserActivityLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AdminAccessLogFilter extends OncePerRequestFilter {

    private final LoggingService loggingService;
    private final HandlerMappingIntrospector handlerMappingIntrospector;

    // 관리자 페이지 패턴 정의 (필요시 확장)
    private static final List<String> ADMIN_PATH_PATTERNS = Arrays.asList(
            "/api/admin/**",
            "/admin/**",
            "/management/**"
    );

    private final RequestMatcher adminMatcher;

    public AdminAccessLogFilter(LoggingService loggingService, HandlerMappingIntrospector handlerMappingIntrospector) {
        this.loggingService = loggingService;
        this.handlerMappingIntrospector = handlerMappingIntrospector;
        this.adminMatcher = buildAdminMatcher(handlerMappingIntrospector);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        // 관리자 페이지 요청만 로깅
        if (adminMatcher.matches(request)) {
            // 응답 래퍼를 사용하여 상태 코드 캡처
            StatusCapturingResponseWrapper responseWrapper = 
                    new StatusCapturingResponseWrapper(response);
            
            try {
                filterChain.doFilter(request, responseWrapper);
            } finally {
                // 요청 처리 후 로그 기록 (성공, 실패 모두)
                logAdminAccess(request, responseWrapper);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 관리자 경로가 아니면 필터를 건너뛴다
        return !adminMatcher.matches(request);
    }
    
    private void logAdminAccess(HttpServletRequest request, 
                               StatusCapturingResponseWrapper response) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            String username = "anonymous";
            boolean isAdmin = false;
            
            if (authentication != null && authentication.isAuthenticated()) {
                username = authentication.getName();
                // 관리자 권한 확인
                isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(auth -> "admin_role".equals(auth.getAuthority()));
            }
            
            int statusCode = response.getStatus();
            
            // 접근 결과 판정
            UserActivityLog.AccessResult accessResult;
            if (statusCode >= 200 && statusCode < 300) {
                accessResult = UserActivityLog.AccessResult.SUCCESS;
            } else if (statusCode == 401) {
                accessResult = UserActivityLog.AccessResult.UNAUTHENTICATED;
            } else if (statusCode == 403) {
                accessResult = UserActivityLog.AccessResult.DENIED;
            } else {
                accessResult = UserActivityLog.AccessResult.ERROR;
            }
            
            // UserActivityRequest 빌더 패턴으로 생성
            UserActivityRequest activityRequest = UserActivityRequest.builder()
                    .username(username)
                    .isAdmin(isAdmin)
                    .activityTime(LocalDateTime.now())
                    .ipAddress(getClientIp(request))
                    .sessionId(getSessionId(request))
                    .pageUrl(buildFullUrl(request))
                    .referrerUrl(request.getHeader("Referer"))
                    .userAgent(request.getHeader("User-Agent"))
                    .targetId(0) // 관리자 로그는 targetId 사용 안함
                    .isAdminAccess(true) // 관리자 페이지 접근임을 표시
                    .accessStatus(statusCode)
                    .accessResult(accessResult)
                    .activityType(UserActivityLog.ActivityType.ADMIN_ACCESS)
                    .build();

            log.info("Calling LoggingService.logSave - User: {}, URI: {}, Status: {}, Result: {}", 
                    username, request.getRequestURI(), statusCode, accessResult);
            
            loggingService.logSave(activityRequest);
            
            // 접근 거부 시 경고 로그
            if (statusCode == 401 || statusCode == 403) {
                log.warn("Admin access denied - User: {}, URI: {}, Status: {}", 
                        username, request.getRequestURI(), statusCode);
            }
            
        } catch (Exception e) {
            log.error("Error logging admin access", e);
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 첫 번째 IP 주소 반환 (프록시 체인의 경우)
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
    
    private String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : null;
    }
    
    private String buildFullUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getRequestURI());
        
        if (request.getQueryString() != null) {
            url.append("?").append(request.getQueryString());
        }
        
        return url.toString();
    }

    private static RequestMatcher buildAdminMatcher(HandlerMappingIntrospector handlerMappingIntrospector) {
        List<RequestMatcher> matchers = ADMIN_PATH_PATTERNS.stream()
                .filter(StringUtils::hasText)
                .map(pattern -> new MvcRequestMatcher(handlerMappingIntrospector, pattern))
                .collect(Collectors.toList());
        Assert.state(!matchers.isEmpty(), "Admin path patterns must not be empty");
        return new OrRequestMatcher(matchers);
    }
    
    /**
     * 응답 상태 코드를 캡처하기 위한 래퍼 클래스
     */
    private static class StatusCapturingResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        private int httpStatus = 200;
        
        public StatusCapturingResponseWrapper(HttpServletResponse response) {
            super(response);
        }
        
        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            this.httpStatus = sc;
        }
        
        @Override
        public void sendError(int sc) throws IOException {
            this.httpStatus = sc;
            super.sendError(sc);
        }
        
        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.httpStatus = sc;
            super.sendError(sc, msg);
        }
        
        public int getStatus() {
            return httpStatus;
        }
    }
}
