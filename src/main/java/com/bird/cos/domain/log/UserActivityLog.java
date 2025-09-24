package com.bird.cos.domain.log;

import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_role_id")
    private UserRole userRole;

    @Column(name = "page_url", length = 2083, nullable = false)
    private String pageUrl;

    @Column(name = "referrer_url", length = 2083)
    private String referrerUrl;

    @Column(name = "activity_time", updatable = false)
    private LocalDateTime activityTime;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "target_id")
    private Long targetId;

}