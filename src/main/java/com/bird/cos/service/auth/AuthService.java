package com.bird.cos.service.auth;

import com.bird.cos.domain.user.User;
import com.bird.cos.exception.ErrorCode;
import com.bird.cos.exception.UnauthorizedException;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User login(String email, String rawPassword) throws UnauthorizedException {
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(rawPassword, user.getUserPassword())) {
            throw new UnauthorizedException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        return user;
    }
}

