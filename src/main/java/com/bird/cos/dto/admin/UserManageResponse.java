package com.bird.cos.dto.admin;

import com.bird.cos.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserManageResponse {
    
    private Long userId;
    private String userName;
    private String userEmail;
    private String userNickname;
    private String userAddress;
    private String userPhone;
    private String socialProvider;
    private String socialId;
    private Boolean termsAgreed;
    private String userRoleName;
    private String roleDescription;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdatedAt;
    
    public static UserManageResponse from(User user) {
        return new UserManageResponse(
                user.getUserId(),
                user.getUserName(),
                user.getUserEmail(),
                user.getUserNickname(),
                user.getUserAddress(),
                user.getUserPhone(),
                user.getSocialProvider(),
                user.getSocialId(),
                user.getTermsAgreed(),
                user.getUserRole() != null ? user.getUserRole().getUserRoleName() : null,
                user.getUserRole() != null ? user.getUserRole().getRoleDescription() : null,
                user.getUserCreatedAt(),
                user.getUserUpdatedAt()
        );
    }

    // 관리자 여부 확인
    public boolean isAdmin() {
        return "ADMIN".equals(this.userRoleName) || "SUPER_ADMIN".equals(this.userRoleName);
    }

    // 일반 사용자 여부 확인
    public boolean isUser() {
        return "USER".equals(this.userRoleName);
    }
}
