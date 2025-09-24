package com.bird.cos.aop;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
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

}
