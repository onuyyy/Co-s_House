package com.bird.cos.domain.admin;

import com.bird.cos.dto.admin.AdminUpdateRequest;
import com.bird.cos.dto.admin.UserUpdateRequest;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ADMIN")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_role_id", nullable = false)
    private AdminRole adminRole;

    @Column(name = "admin_password", length = 250, nullable = false)
    private String adminPassword;

    @Column(name = "admin_name", length = 30, nullable = false)
    private String adminName;

    @Column(name = "admin_email", length = 100, unique = true, nullable = false)
    private String adminEmail;

    @Column(name = "admin_phone", length = 20)
    private String adminPhone;

    @Column(name = "admin_status", length = 10, nullable = false)
    private String adminStatus;

    @Column(name = "admin_created_date", nullable = false, insertable = false, updatable = false)
    private LocalDateTime adminCreatedDate;

    @Column(name = "admin_updated_date")
    private LocalDateTime adminUpdatedDate;

    public void update(AdminUpdateRequest request) {
        if (request.getAdminName() != null) this.adminName = request.getAdminName();
        if (request.getAdminPhone() != null) this.adminPhone = request.getAdminPhone();
        if (request.getAdminEmail() != null) this.adminEmail = request.getAdminEmail();
        if (request.getAdminStatus() != null) this.adminStatus = request.getAdminStatus();
        if (request.getAdminRole() != null) this.adminRole = request.getAdminRole();
    }

}