package com.bird.cos.dto.admin;

import com.bird.cos.domain.log.UserActivityLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAccessLogDto {
    
    private Long id;
    private String userName;
    private String userEmail;
    private String userRole;
    private String pageUrl;
    private String ipAddress;
    private LocalDateTime activityTime;
    private String activityTimeFormatted;
    private Integer accessStatus;
    private UserActivityLog.AccessResult accessResult;
    private String accessResultDisplay;
    private String accessResultClass;
    
    public static AdminAccessLogDto from(UserActivityLog log) {
        String userName = log.getUserId() != null ? log.getUserId().getUserName() : "비회원";
        String userEmail = log.getUserId() != null ? log.getUserId().getUserEmail() : "-";
        String userRole = log.getUserRole() != null ? log.getUserRole().getUserRoleName() : "-";
        
        return AdminAccessLogDto.builder()
                .id(log.getUserActivityLogId())
                .userName(userName)
                .userEmail(userEmail)
                .userRole(userRole)
                .pageUrl(log.getPageUrl())
                .ipAddress(log.getIpAddress())
                .activityTime(log.getActivityTime())
                .activityTimeFormatted(formatDateTime(log.getActivityTime()))
                .accessStatus(log.getAccessStatus())
                .accessResult(log.getAccessResult())
                .accessResultDisplay(getAccessResultDisplay(log.getAccessResult()))
                .accessResultClass(getAccessResultClass(log.getAccessResult()))
                .build();
    }
    
    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    private static String getAccessResultDisplay(UserActivityLog.AccessResult result) {
        if (result == null) return "알 수 없음";
        return switch (result) {
            case SUCCESS -> "성공";
            case DENIED -> "권한 거부";
            case UNAUTHENTICATED -> "미인증";
            case ERROR -> "오류";
        };
    }
    
    private static String getAccessResultClass(UserActivityLog.AccessResult result) {
        if (result == null) return "secondary";
        return switch (result) {
            case SUCCESS -> "success";
            case DENIED -> "danger";
            case UNAUTHENTICATED -> "warning";
            case ERROR -> "dark";
        };
    }
}
