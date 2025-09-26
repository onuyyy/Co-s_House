package com.bird.cos.security.oauth;

import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserRole;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.repository.user.UserRoleRepository;
import com.bird.cos.security.AuthorityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuth2UserService extends DefaultOAuth2UserService {

    private static final String REGISTRATION_ID = "kakao";
    private static final String PROVIDER = "KAKAO";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthorityService authorityService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User delegate = super.loadUser(userRequest);

        if (!REGISTRATION_ID.equals(userRequest.getClientRegistration().getRegistrationId())) {
            return delegate;
        }

        KakaoProfile profile = KakaoProfile.from(delegate.getAttributes());
        log.debug("Loaded Kakao profile id={}, email={}", profile.id(), profile.email());
        User user = syncUserWith(profile);

        var authorities = authorityService.resolveAuthoritiesFor(user);
        Map<String, Object> attributes = new LinkedHashMap<>(delegate.getAttributes());
        attributes.put("userId", user.getUserId());
        attributes.put("userEmail", user.getUserEmail());
        attributes.put("userNickname", user.getUserNickname());

        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(authorities, attributes, nameAttributeKey);
    }

    private User syncUserWith(KakaoProfile profile) {
        String kakaoId = profile.id();
        if (kakaoId == null || kakaoId.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info"), "Kakao 사용자 정보에 식별자가 없습니다.");
        }
        String email = resolveEmail(profile, kakaoId);

        Optional<User> bySocial = userRepository.findBySocialProviderAndSocialId(PROVIDER, kakaoId);
        Optional<User> byEmail = email == null ? Optional.empty() : userRepository.findByUserEmail(email);

        User user = bySocial.or(() -> byEmail).orElse(null);

        if (user == null) {
            user = createNewUser(profile, kakaoId, email);
        } else {
            updateExistingUser(user, kakaoId, email, profile);
        }

        User saved = userRepository.save(user);
        log.debug("Synchronized Kakao user id={}, email={}", saved.getUserId(), saved.getUserEmail());
        return saved;
    }

    private User createNewUser(KakaoProfile profile, String kakaoId, String email) {
        UserRole defaultRole = getDefaultUserRole();
        String nickname = generateUniqueNickname(profile.nickname(), kakaoId);
        String name = (profile.name() != null && !profile.name().isBlank()) ? profile.name().trim() : nickname;

        return User.builder()
                .userEmail(email)
                .userNickname(nickname)
                .userName(name)
                .userPassword(null)
                .userRole(defaultRole)
                .socialProvider(PROVIDER)
                .socialId(kakaoId)
                .termsAgreed(true)
                .emailVerified(true)
                .build();
    }

    private void updateExistingUser(User user, String kakaoId, String email, KakaoProfile profile) {
        user.updateEmail(email);
        user.linkSocialAccount(PROVIDER, kakaoId);
        user.markEmailVerified();
        user.agreeTerms();

        if (user.getUserRole() == null) {
            user.updateUserRole(getDefaultUserRole());
        }

        if (user.getUserNickname() == null || user.getUserNickname().trim().isEmpty()) {
            user.updateNickname(generateUniqueNickname(profile.nickname(), kakaoId));
        }

        user.updateNameIfBlank(profile.name());
    }

    private String resolveEmail(KakaoProfile profile, String kakaoId) {
        if (profile.email() != null && !profile.email().isBlank()) {
            return profile.email().trim();
        }
        return "kakao_" + kakaoId + "@kakao-user.local";
    }

    private String generateUniqueNickname(String sourceNickname, String kakaoId) {
        String base;
        if (sourceNickname != null && !sourceNickname.trim().isEmpty()) {
            base = sourceNickname.trim();
        } else if (kakaoId != null && !kakaoId.isBlank()) {
            int take = Math.min(4, kakaoId.length());
            base = "kakao_" + kakaoId.substring(kakaoId.length() - take);
        } else {
            base = "kakao_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        }

        String candidate = base;
        int suffix = 1;
        while (userRepository.findByUserNickname(candidate).isPresent()) {
            candidate = base + suffix;
            suffix++;
            if (suffix > 50) {
                candidate = "kakao_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                break;
            }
        }
        return candidate;
    }

    private UserRole getDefaultUserRole() {
        return userRoleRepository.findByUserRoleName("USER")
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("server_error"), "기본 USER 권한이 존재하지 않습니다."));
    }

    private record KakaoProfile(String id, String email, String nickname, String name) {

        @SuppressWarnings("unchecked")
        private static KakaoProfile from(Map<String, Object> attributes) {
            Object idValue = attributes.get("id");
            String id = idValue == null ? null : String.valueOf(idValue);

            Map<String, Object> account = attributes.get("kakao_account") instanceof Map
                    ? (Map<String, Object>) attributes.get("kakao_account")
                    : Map.of();

            Map<String, Object> profile = account.get("profile") instanceof Map
                    ? (Map<String, Object>) account.get("profile")
                    : Map.of();

            String email = account.get("email") instanceof String ? (String) account.get("email") : null;
            String nickname = profile.get("nickname") instanceof String ? (String) profile.get("nickname") : null;
            String name = account.get("name") instanceof String ? (String) account.get("name") : null;

            return new KakaoProfile(id, email, nickname, name);
        }
    }
}
