package com.bird.cos.security;

import com.bird.cos.domain.user.UserRole;
import com.bird.cos.domain.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 사용자 역할 기반 권한 계산
 * - 기본: user_role
 * - User.role(userRoleName) 값에 따라 추가 권한 부여(예: ADMIN -> admin_role)
 */
@Component
public class AuthorityService {

    public List<GrantedAuthority> resolveAuthoritiesFor(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("user_role"));

        UserRole role = user == null ? null : user.getUserRole();
        if (role != null && role.getUserRoleName() != null) {
            String name = role.getUserRoleName().trim().toUpperCase(Locale.ROOT);
            if ("ADMIN".equals(name) || "SUPER_ADMIN".equals(name)) {
                authorities.add(new SimpleGrantedAuthority("admin_role"));
            }
        }
        return authorities;
    }
}
