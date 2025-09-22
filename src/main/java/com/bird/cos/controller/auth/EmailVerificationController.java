package com.bird.cos.controller.auth;

import com.bird.cos.dto.auth.EmailVerificationConfirmRequest;
import com.bird.cos.dto.auth.EmailVerificationSendRequest;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.service.auth.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/auth/email/verification")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;

    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> requestVerification(@Valid @RequestBody EmailVerificationSendRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.findByUserEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "이미 가입된 이메일입니다."));
        }

        try {
            emailVerificationService.sendVerificationCode(email, request.name());
            return ResponseEntity.ok(Map.of("message", "인증번호를 이메일로 발송했습니다."));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.",
                            "detail", ex.getMessage()
                    ));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmVerification(@Valid @RequestBody EmailVerificationConfirmRequest request) {
        String email = normalizeEmail(request.email());
        boolean verified = emailVerificationService.verifyCode(email, request.code());

        if (verified) {
            return ResponseEntity.ok(Map.of(
                    "verified", true,
                    "message", "이메일 인증이 완료되었습니다."
            ));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "verified", false,
                        "message", "인증번호가 일치하지 않거나 만료되었습니다."
                ));
    }

    private String normalizeEmail(String rawEmail) {
        return rawEmail == null ? null : rawEmail.trim().toLowerCase(Locale.ROOT);
    }
}
