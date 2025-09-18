package com.bird.cos.service.auth;

import com.bird.cos.domain.user.User;
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
                .orElseThrow(() -> new UnauthorizedException("invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getUserPassword())) {
            throw new UnauthorizedException("invalid credentials");
        }

        return user;
    }
}

