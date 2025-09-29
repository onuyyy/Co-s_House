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
public class SocialOAuth2UserService extends DefaultOAuth2UserService {

    private static final String REG_KAKAO = "kakao";
    private static final String REG_NAVER = "naver";

    private static final String PROVIDER_KAKAO = "KAKAO";
    private static final String PROVIDER_NAVER = "NAVER";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthorityService authorityService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User delegate = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        SocialProfile profile = SocialProfile.from(registrationId, delegate.getAttributes());

        if (profile == null) {
            return delegate;
        }

        User user = syncUserWith(profile);

        var authorities = authorityService.resolveAuthoritiesFor(user);
        Map<String, Object> attributes = new LinkedHashMap<>(delegate.getAttributes());
        attributes.put("userId", user.getUserId());
        attributes.put("userEmail", user.getUserEmail());
        attributes.put("userNickname", user.getUserNickname());

        // Naver 응답은 response 맵 안에 값이 있으므로 평탄화
        if (REG_NAVER.equals(registrationId)) {
            Object responseMap = attributes.get("response");
            if (responseMap instanceof Map<?, ?> response) {
                response.forEach((key, value) -> attributes.put(String.valueOf(key), value));
            }
        }

        String nameAttributeKey = resolveNameAttributeKey(registrationId,
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName());

        // nameAttributeKey가 맵 키와 일치하도록 보장
        if (!attributes.containsKey(nameAttributeKey) && profile.socialId() != null) {
            attributes.put(nameAttributeKey, profile.socialId());
        }

        return new DefaultOAuth2User(authorities, attributes, nameAttributeKey);
    }

    private String resolveNameAttributeKey(String registrationId, String configuredKey) {
        if (REG_NAVER.equals(registrationId)) {
            return "id";
        }
        return configuredKey;
    }

    private User syncUserWith(SocialProfile profile) {
        String provider = profile.provider();
        String socialId = profile.socialId();
        if (socialId == null || socialId.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info"), provider + " 사용자 식별자를 찾을 수 없습니다.");
        }

        String email = resolveEmail(profile);

        Optional<User> bySocial = userRepository.findBySocialProviderAndSocialId(provider, socialId);
        Optional<User> byEmail = email == null ? Optional.empty() : userRepository.findByUserEmail(email);

        User user = bySocial.or(() -> byEmail).orElse(null);

        if (user == null) {
            user = createNewUser(profile, provider, socialId, email);
        } else {
            updateExistingUser(user, provider, socialId, email, profile);
        }

        User saved = userRepository.save(user);
        log.debug("[{}] OAuth2 user synchronized. userId={}, email={}", provider, saved.getUserId(), saved.getUserEmail());
        return saved;
    }

    private User createNewUser(SocialProfile profile, String provider, String socialId, String email) {
        UserRole defaultRole = getDefaultUserRole();
        String nickname = generateUniqueNickname(profile.nickname(), socialId, provider);
        String name = (profile.name() != null && !profile.name().isBlank()) ? profile.name().trim() : nickname;

        return User.builder()
                .userEmail(email)
                .userNickname(nickname)
                .userName(name)
                .userPassword(null)
                .userRole(defaultRole)
                .socialProvider(provider)
                .socialId(socialId)
                .termsAgreed(true)
                .emailVerified(true)
                .build();
    }

    private void updateExistingUser(User user, String provider, String socialId, String email, SocialProfile profile) {
        user.updateEmail(email);
        user.linkSocialAccount(provider, socialId);
        user.markEmailVerified();
        user.agreeTerms();

        if (user.getUserRole() == null) {
            user.updateUserRole(getDefaultUserRole());
        }

        if (user.getUserNickname() == null || user.getUserNickname().trim().isEmpty()) {
            user.updateNickname(generateUniqueNickname(profile.nickname(), socialId, provider));
        }

        user.updateNameIfBlank(profile.name());
    }

    private UserRole getDefaultUserRole() {
        return userRoleRepository.findFirstByUserRoleNameOrderByUserRoleIdAsc("USER")
                .orElseThrow(() -> new OAuth2AuthenticationException(new OAuth2Error("server_error"), "기본 USER 권한이 존재하지 않습니다."));
    }

    private String resolveEmail(SocialProfile profile) {
        if (profile.email() != null && !profile.email().isBlank()) {
            return profile.email().trim();
        }
        return profile.provider().toLowerCase() + "_" + profile.socialId() + "@social-user.local";
    }

    private String generateUniqueNickname(String sourceNickname, String socialId, String provider) {
        String base;
        if (sourceNickname != null && !sourceNickname.trim().isEmpty()) {
            base = sourceNickname.trim();
        } else if (socialId != null && !socialId.isBlank()) {
            int take = Math.min(4, socialId.length());
            base = provider.toLowerCase() + "_" + socialId.substring(socialId.length() - take);
        } else {
            base = provider.toLowerCase() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        }

        String candidate = base;
        int suffix = 1;
        while (userRepository.findByUserNickname(candidate).isPresent()) {
            candidate = base + suffix;
            suffix++;
            if (suffix > 50) {
                candidate = provider.toLowerCase() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                break;
            }
        }
        return candidate;
    }

    private record SocialProfile(String registrationId,
                                 String provider,
                                 String socialId,
                                 String email,
                                 String nickname,
                                 String name) {

        @SuppressWarnings("unchecked")
        private static SocialProfile from(String registrationId, Map<String, Object> attributes) {
            if (registrationId == null) {
                return null;
            }

            if (REG_KAKAO.equals(registrationId)) {
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

                return new SocialProfile(registrationId, PROVIDER_KAKAO, id, email, nickname, name);
            }

            if (REG_NAVER.equals(registrationId)) {
                Map<String, Object> response = attributes.get("response") instanceof Map
                        ? (Map<String, Object>) attributes.get("response")
                        : Map.of();

                String id = response.get("id") instanceof String ? (String) response.get("id") : null;
                String email = response.get("email") instanceof String ? (String) response.get("email") : null;
                String nickname = response.get("nickname") instanceof String ? (String) response.get("nickname") : null;
                String name = response.get("name") instanceof String ? (String) response.get("name") : null;

                return new SocialProfile(registrationId, PROVIDER_NAVER, id, email, nickname, name);
            }

            return null;
        }

        public String socialId() {
            return socialId;
        }
    }
}
