package com.bird.cos.domain.log;

import com.bird.cos.domain.admin.Admin;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ADMIN_ACTION_LOG")
public class AdminActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_log_id")
    private Long adminLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin adminId;

    @Column(name = "admin_name", length = 100, nullable = false)
    private String adminName;

    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(name = "target_resource", length = 100)
    private String targetResource;

    @Column(name = "target_resource_id", length = 100)
    private String targetResourceId;

    @Column(name = "action_detail", columnDefinition = "TEXT")
    private String actionDetail;

    @Column(name = "request_ip", length = 50, nullable = false)
    private String requestIp;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ActionType {
        CREATE, UPDATE, DELETE, LOGIN, LOGOUT
    }

    public enum Status {
        SUCCESS, FAILURE
    }

}