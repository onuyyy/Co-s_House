package com.bird.cos.dto.admin;

import com.bird.cos.domain.log.UserActivityLog;
import com.bird.cos.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogResponse {

    private Long activityLogId;
    private String username;
    private String userEmail;
    private String userRole;
    private LocalDateTime activityTime;
    private String ipAddress;
    private String sessionId;
    private String pageUrl;
    private String referrerUrl;
    private String userAgent;

    public static LogResponse from(UserActivityLog log) {
        User user = log.getUserId();

        return LogResponse.builder()
                .activityLogId(log.getUserActivityLogId())
                .username(user != null ? user.getUserName() : "anonymous")
                .userEmail(user != null ? user.getUserEmail() : "")
                .userRole(log.getUserRole() != null ? log.getUserRole().getUserRoleName() : "UNKNOWN")
                .activityTime(log.getActivityTime())
                .ipAddress(log.getIpAddress())
                .sessionId(log.getSessionId())
                .pageUrl(log.getPageUrl())
                .referrerUrl(log.getReferrerUrl())
                .userAgent(log.getUserAgent())
                .build();
    }
}
