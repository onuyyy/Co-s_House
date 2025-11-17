package com.bird.cos.aop;

import com.bird.cos.domain.log.UserActivityLog;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class UserActivityRequest {

    private String username;
    private boolean isAdmin;
    private LocalDateTime activityTime;
    private String ipAddress;
    private String sessionId;
    private String pageUrl;
    private String referrerUrl;
    private String userAgent;
    private long targetId;
    private boolean isAdminAccess;
    private Integer accessStatus;
    private UserActivityLog.AccessResult accessResult;
    private UserActivityLog.ActivityType activityType;

}
