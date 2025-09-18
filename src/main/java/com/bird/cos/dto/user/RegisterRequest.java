package com.bird.cos.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "이름은 필수입니다.")
        @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다.")
        String name,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
        String nickname,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상이어야 합니다.")
        String password,

        @NotBlank(message = "연락처는 필수입니다.")
        // 한국 휴대폰 포맷(010-1234-5678 or 01012345678) 정도만 간단히 체크
        @Pattern(regexp = "^0\\d{1,2}-?\\d{3,4}-?\\d{4}$", message = "연락처 형식이 올바르지 않습니다.")
        String phone,

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
        String address
) {}

