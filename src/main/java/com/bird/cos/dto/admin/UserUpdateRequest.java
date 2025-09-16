package com.bird.cos.dto.admin;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserUpdateRequest {

    private String userEmail;
    private String userNickname;
    private String userName;
    private String userAddress;
    private String userPhone;
    private String socialProvider;
    private String socialId;
    private Boolean termsAgreed;

}
