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

    @Column(name = "is_admin_access", nullable = false)
    private Boolean isAdminAccess = false;

    @Column(name = "access_status")
    private Integer accessStatus;  // HTTP 상태 코드 (200, 401, 403 등)

    @Column(name = "access_result", length = 50)
    @Enumerated(EnumType.STRING)
    private AccessResult accessResult;  // SUCCESS, DENIED, UNAUTHENTICATED

    @Column(name = "activity_type", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityType activityType; // 행위 유형 (LOGIN, VIEW, ADMIN_ACCESS 등)

    public enum AccessResult {
        SUCCESS,         // 정상 접근
        DENIED,          // 권한 없음 (403)
        UNAUTHENTICATED, // 인증되지 않음 (401)
        ERROR            // 기타 오류
    }

    public enum ActivityType {
        ADMIN_ACCESS,
        LOGIN,
        VIEW,
        PURCHASE,
        LOGOUT
    }

}
