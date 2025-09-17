package com.bird.cos.dto.user;

import com.bird.cos.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private String email;
    private String nickname;
    private String name;
    private String address;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .email(user.getUserEmail())
                .nickname(user.getUserNickname())
                .name(user.getUserName())
                .address(user.getUserAddress())
                .phone(user.getUserPhone())
                .createdAt(user.getUserCreatedAt())
                .updatedAt(user.getUserUpdatedAt())
                .build();
    }
}

