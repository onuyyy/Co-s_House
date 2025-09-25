package com.bird.cos.service.register;

import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserRole;
import com.bird.cos.dto.user.RegisterRequest;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.repository.user.UserRoleRepository;
import com.bird.cos.service.auth.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public User register(RegisterRequest req) {
        String normalizedEmail = req.email().trim().toLowerCase(Locale.ROOT);

        // 이메일 중복 체크
        if (userRepository.findByUserEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        // 닉네임 중복 체크
        if (userRepository.findByUserNickname(req.nickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        if (!emailVerificationService.isVerified(normalizedEmail)) {
            throw new IllegalStateException("이메일 인증을 완료해주세요.");
        }

        UserRole defaultRole = userRoleRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("기본 역할(id=1)을 찾을 수 없습니다."));

        User user = User.builder()
                .userRole(defaultRole)
                .userEmail(normalizedEmail)
                .userPassword(passwordEncoder.encode(req.password()))
                .userNickname(req.nickname())
                .userName(req.name())
                .userAddress(req.address())
                .userPhone(req.phone())
                .emailVerified(true)
                .termsAgreed(Boolean.TRUE)
                .build();

        User savedUser = userRepository.save(user);
        emailVerificationService.consumeVerification(normalizedEmail);
        return savedUser;
    }
}
