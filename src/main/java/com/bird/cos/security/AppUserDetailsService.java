package com.bird.cos.security;

import com.bird.cos.domain.user.User;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 사용자 인증 세부 정보 서비스
 * cos: 이메일을 사용자명으로 간주하여 조회합니다.
 */
@Service
@Validated
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuthorityService authorityService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username을 이메일로 간주하고 조회 (없으면 예외)
        User user = userRepository.findByUserEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        var authorities = authorityService.resolveAuthoritiesFor(user);

        return CustomUserDetails.builder()
                .authorities(authorities)
                .name(user.getUserName())
                .nickname(user.getUserNickname())
                .userEmail(user.getUserEmail())
                .password(user.getUserPassword())
                .build();
    }
}
