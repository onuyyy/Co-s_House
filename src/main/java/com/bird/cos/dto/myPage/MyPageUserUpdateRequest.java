package com.bird.cos.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyPageUserUpdateRequest {
    private String userNickname;
    private String userPhone;
    private String userAddress;
    private String userPassword;
}
