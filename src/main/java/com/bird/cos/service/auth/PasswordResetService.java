package com.bird.cos.service.auth;

import com.bird.cos.domain.user.User;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;

    public void request(String email) {
        String normalizedEmail = email.toLowerCase();
        User user = userRepository.findByUserEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("가입된 회원 정보를 찾을 수 없습니다."));

        emailVerificationService.sendVerificationCode(normalizedEmail, user.getUserName());
    }

    @Transactional
    public void reset(String email, String code, String rawPassword) {
        boolean verified = emailVerificationService.verifyCode(email, code);
        if (!verified) {
            throw new IllegalArgumentException("인증번호가 올바르지 않습니다.");
        }

        User user = userRepository.findByUserEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        user.updatePassword(passwordEncoder.encode(rawPassword));
        emailVerificationService.consumeVerification(email);
    }
}
