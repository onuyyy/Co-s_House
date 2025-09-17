package com.bird.cos.dto.admin;

import com.bird.cos.domain.admin.AdminRole;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AdminUpdateRequest {

    private Long adminId;
    private AdminRole adminRole;
    private String adminName;
    private String adminEmail;
    private String adminPhone;
    private String adminStatus;
}
