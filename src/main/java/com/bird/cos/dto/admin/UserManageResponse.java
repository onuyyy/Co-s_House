package com.bird.cos.dto.admin;

import com.bird.cos.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserResponse {
    private String userEmail;
    private String userNickname;
    private String userName;
    private String userAddress;
    private String userPhone;
    private String socialProvider;
    private String socialId;
    private Boolean termsAgreed;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdatedAt;

    public static AdminUserResponse from(User user) {
        return AdminUserResponse.builder()
                .userEmail(user.getUserEmail())
                .userNickname(user.getUserNickname())
                .userName(user.getUserName())
                .userAddress(user.getUserAddress())
                .userPhone(user.getUserPhone())
                .socialProvider(user.getSocialProvider())
                .socialId(user.getSocialId())
                .termsAgreed(user.getTermsAgreed())
                .userCreatedAt(user.getUserCreatedAt())
                .userUpdatedAt(user.getUserUpdatedAt())
                .build();
    }
}
