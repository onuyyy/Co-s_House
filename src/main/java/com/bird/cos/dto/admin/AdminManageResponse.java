package com.bird.cos.dto.admin;

import com.bird.cos.domain.admin.Admin;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminManageResponse {

    private Long adminId;
    private String adminRole;
    private String adminName;
    private String adminEmail;
    private String adminPhone;
    private String adminStatus;
    private LocalDateTime adminCreatedDate;
    private LocalDateTime adminUpdatedDate;

    public static AdminManageResponse from(Admin admin) {
        return AdminManageResponse.builder()
                .adminId(admin.getAdminId())
                .adminRole(admin.getAdminRole().getAdminRoleName())
                .adminName(admin.getAdminName())
                .adminEmail(admin.getAdminEmail())
                .adminPhone(admin.getAdminPhone())
                .adminStatus(admin.getAdminStatus())
                .adminCreatedDate(admin.getAdminCreatedDate())
                .adminUpdatedDate(admin.getAdminUpdatedDate())
                .build();
    }
}
