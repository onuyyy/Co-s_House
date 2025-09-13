package com.bird.cos.domain.log;

import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_ACTIVITY_LOG")
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_activity_log_id")
    private Long userActivityLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;

    @Column(name = "page_url", length = 2083, nullable = false)
    private String pageUrl;

    @Column(name = "referrer_url", length = 2083)
    private String referrerUrl;

    @Column(name = "activity_time", insertable = false, updatable = false)
    private LocalDateTime activityTime;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "activity_created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime activityCreatedAt;

    @Column(name = "activity_type", length = 50, nullable = false)
    private String activityType;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Column(name = "target_id")
    private Long targetId;

}