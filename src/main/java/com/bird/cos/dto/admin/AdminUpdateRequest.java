package com.bird.cos.dto.admin;

import com.bird.cos.domain.user.UserRole;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AdminUpdateRequest {

    private Long adminId;
    private UserRole adminRole;
    private String adminName;
    private String adminNickName;
    private String adminEmail;
    private String adminPhone;
    private String adminAddress;
}