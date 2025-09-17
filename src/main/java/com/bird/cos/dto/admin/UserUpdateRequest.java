package com.bird.cos.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    private String userName;
    private String userEmail;
    private String userNickname;
    private String userAddress;
    private String userPhone;
    private Boolean termsAgreed;
    private Long userRoleId; // 역할 변경을 위한 필드
}
