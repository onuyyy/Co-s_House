package com.bird.cos.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Admin Controller 호출 시점에 로깅
 * 비로그인 / 사용자 / 관리자
 * LoggingService 에서 처리
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Aspect
public class LoggingAspect {

    private final LoggingService loggingService;
    private final HttpServletRequest request;

    @Before("execution(* com.bird.cos.controller.admin..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        try {
            String ip = request.getRemoteAddr(); // 클라이언트 ip
            String userAgent = request.getHeader("User-Agent"); // 브라우저/OS
            String referrerUrl = request.getHeader("Referer"); // 이전 페이지 url
            String pageUrl = request.getRequestURL().toString();
            String sessionId = request.getSession().getId();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            UserActivityRequest userActivityRequest = new UserActivityRequest();
            userActivityRequest.setIpAddress(ip);
            userActivityRequest.setUserAgent(userAgent);
            userActivityRequest.setReferrerUrl(referrerUrl);
            userActivityRequest.setPageUrl(pageUrl);
            userActivityRequest.setSessionId(sessionId);
            userActivityRequest.setActivityTime(LocalDateTime.now());

            String username;

            if (authentication != null) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof UserDetails userDetails) {
                    username = userDetails.getUsername();

                    boolean isAdmin = userDetails.getAuthorities().stream()
                            .anyMatch(authority ->
                                    authority.getAuthority().contains("ADMIN"));

                    userActivityRequest.setUsername(username);
                    userActivityRequest.setAdmin(isAdmin);
                }

            } else {
                userActivityRequest.setUsername("anonymous");
                userActivityRequest.setAdmin(false);
            }

            loggingService.logSave(userActivityRequest);
        } catch (Exception e) {
            // todo : 로그 exception
        }
    }
}
