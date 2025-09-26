package com.bird.cos.security.oauth;

import com.bird.cos.domain.user.User;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.security.AuthorityService;
import com.bird.cos.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthorityService authorityService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (authentication instanceof OAuth2AuthenticationToken token) {
            Map<String, Object> attributes = token.getPrincipal().getAttributes();
            Optional<User> userOptional = resolveUser(attributes);
            userOptional.ifPresent(user -> {
                syncSecurityContext(user, token, request, response);
                syncSessionAttributes(request.getSession(true), user);
            });
        }

        redirectStrategy.sendRedirect(request, response, "/");
    }

    private Optional<User> resolveUser(Map<String, Object> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            log.warn("OAuth2 attributes empty, cannot resolve user");
            return Optional.empty();
        }

        Long userId = extractLong(attributes.get("userId"));
        String email = extractString(attributes.get("userEmail"));

        Optional<User> optional = Optional.empty();
        if (userId != null) {
            optional = userRepository.findWithRoleByUserId(userId);
        }
        if (optional.isEmpty() && email != null) {
            optional = userRepository.findWithRoleByEmail(email);
        }

        if (optional.isEmpty()) {
            log.warn("OAuth2 user not found. attributes={}", attributes);
        }
        return optional;
    }

    private void syncSessionAttributes(HttpSession session, User user) {
        if (session == null) {
            return;
        }

        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userEmail", user.getUserEmail());
        session.setAttribute("userName", user.getUserName());
        session.setAttribute("user", user);
        log.debug("OAuth2 login session synchronized for userId={} email={}", user.getUserId(), user.getUserEmail());
    }

    private void syncSecurityContext(User user,
                                     OAuth2AuthenticationToken originalToken,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        Map<String, Object> attributes = originalToken.getPrincipal().getAttributes();
        String email = user.getUserEmail();
        String nickname = user.getUserNickname();
        String name = user.getUserName();

        var authorities = authorityService.resolveAuthoritiesFor(user);
        CustomUserDetails principal = CustomUserDetails.builder()
                .userEmail(email)
                .nickname(nickname)
                .name(name)
                .userName(name)
                .password(null)
                .authorities(authorities)
                .build();

        UsernamePasswordAuthenticationToken converted = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );

        converted.setDetails(attributes);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(converted);
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
    }

    private Long extractLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String extractString(Object value) {
        if (value instanceof String str && !str.isBlank()) {
            return str.trim();
        }
        return null;
    }
}
