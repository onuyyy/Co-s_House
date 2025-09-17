package com.bird.cos.dto.admin;

import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminManageResponse {

    private Long adminId;
    private UserRole adminRole;
    private String adminName;
    private String adminNickName;
    private String adminEmail;
    private String adminPhone;
    private String adminAddress;
    private LocalDateTime adminCreatedDate;
    private LocalDateTime adminUpdatedDate;

    public static AdminManageResponse from(User admin) {
        return AdminManageResponse.builder()
                .adminId(admin.getUserId())
                .adminRole(admin.getUserRole())
                .adminName(admin.getUserName())
                .adminNickName(admin.getUserNickname())
                .adminEmail(admin.getUserEmail())
                .adminPhone(admin.getUserPhone())
                .adminAddress(admin.getUserAddress())
                .adminCreatedDate(admin.getUserCreatedAt())
                .adminUpdatedDate(admin.getUserUpdatedAt())
                .build();
    }
}