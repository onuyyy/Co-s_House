package com.bird.cos.controller.account;

import com.bird.cos.dto.user.PasswordResetConfirmRequest;
import com.bird.cos.dto.user.PasswordResetEmailRequest;
import com.bird.cos.service.auth.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/account/reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping
    public String passwordResetPage() {
        return "account/password-reset";
    }

    @PostMapping("/request")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> requestReset(@Valid @RequestBody PasswordResetEmailRequest request) {
        passwordResetService.request(request.email());
        return ResponseEntity.ok(Map.of("message", "입력하신 이메일로 인증번호를 전송했습니다."));
    }

    @PostMapping("/complete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        try {
            passwordResetService.reset(request.email(), request.code(), request.password());
            return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
