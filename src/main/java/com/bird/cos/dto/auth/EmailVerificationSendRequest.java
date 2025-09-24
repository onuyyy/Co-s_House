package com.bird.cos.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationSendRequest(
        @NotBlank @Email String email,
        String name
) {
}
