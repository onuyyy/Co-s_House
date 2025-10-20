package com.bird.cos.security.oauth;

import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserRole;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.repository.user.UserRoleRepository;
import com.bird.cos.security.AuthorityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private AuthorityService authorityService;

    @InjectMocks
    private SocialOAuth2UserService socialOAuth2UserService;

    private Class<?> socialProfileClass;
    private Constructor<?> socialProfileCtor;

    @BeforeEach
    void setUpReflection() throws Exception {
        socialProfileClass = Class.forName("com.bird.cos.security.oauth.SocialOAuth2UserService$SocialProfile");
        socialProfileCtor = socialProfileClass.getDeclaredConstructor(
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class
        );
        socialProfileCtor.setAccessible(true);
    }

    @Test
    void syncUserWith_NewSocialUser_CreatesUserWithDefaults() throws Exception {
        // 소셜 신규 가입 시 사용자 생성과 기본 역할/필드 설정 여부 확인
        Object profile = socialProfileCtor.newInstance(
                "kakao",
                "KAKAO",
                "12345",
                "kakao@example.com",
                "카카오닉",
                "카카오유저"
        );

        UserRole defaultRole = mock(UserRole.class);

        when(userRepository.findBySocialProviderAndSocialId("KAKAO", "12345")).thenReturn(Optional.empty());
        when(userRepository.findByUserEmail("kakao@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUserNickname("카카오닉")).thenReturn(Optional.empty());
        when(userRoleRepository.findFirstByUserRoleNameOrderByUserRoleIdAsc("USER"))
                .thenReturn(Optional.of(defaultRole));

        ArgumentCaptor<User> savedCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(savedCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        User result = ReflectionTestUtils.invokeMethod(socialOAuth2UserService, "syncUserWith", profile);

        User saved = savedCaptor.getValue();
        assertThat(saved.getUserEmail()).isEqualTo("kakao@example.com");
        assertThat(saved.getUserNickname()).isEqualTo("카카오닉");
        assertThat(saved.getUserName()).isEqualTo("카카오유저");
        assertThat(saved.getUserRole()).isEqualTo(defaultRole);
        assertThat(saved.getSocialProvider()).isEqualTo("KAKAO");
        assertThat(saved.getSocialId()).isEqualTo("12345");
        assertThat(saved.getTermsAgreed()).isTrue();
        assertThat(saved.isEmailVerified()).isTrue();
        assertThat(result).isSameAs(saved);
    }

    @Test
    void syncUserWith_ExistingUserByEmail_LinksSocialAndFillsMissingFields() throws Exception {
        // 기존 이메일 사용자와 소셜 계정 연동 시 누락된 필드를 보완하는지 검증
        Object profile = socialProfileCtor.newInstance(
                "kakao",
                "KAKAO",
                "67890",
                "linked@example.com",
                "새닉",
                "새이름"
        );

        User existing = User.builder()
                .userId(42L)
                .userEmail("linked@example.com")
                .termsAgreed(false)
                .emailVerified(false)
                .userRole(null)
                .userNickname(null)
                .userName(null)
                .build();

        UserRole defaultRole = mock(UserRole.class);

        when(userRepository.findBySocialProviderAndSocialId("KAKAO", "67890")).thenReturn(Optional.empty());
        when(userRepository.findByUserEmail("linked@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.findByUserNickname(anyString())).thenReturn(Optional.empty());
        when(userRoleRepository.findFirstByUserRoleNameOrderByUserRoleIdAsc("USER"))
                .thenReturn(Optional.of(defaultRole));
        when(userRepository.save(existing)).thenReturn(existing);

        User result = ReflectionTestUtils.invokeMethod(socialOAuth2UserService, "syncUserWith", profile);

        assertThat(existing.getSocialProvider()).isEqualTo("KAKAO");
        assertThat(existing.getSocialId()).isEqualTo("67890");
        assertThat(existing.getUserRole()).isEqualTo(defaultRole);
        assertThat(existing.getUserNickname()).isNotBlank();
        assertThat(existing.getUserName()).isEqualTo("새이름");
        assertThat(existing.getTermsAgreed()).isTrue();
        assertThat(existing.isEmailVerified()).isTrue();
        assertThat(result).isSameAs(existing);
    }

    @Test
    void syncUserWith_MissingDefaultRole_ThrowsOAuth2AuthenticationException() throws Exception {
        // 기본 USER 역할이 없으면 예외가 발생해야 한다
        Object profile = socialProfileCtor.newInstance(
                "kakao",
                "KAKAO",
                "no-role",
                "missing@example.com",
                "닉",
                "이름"
        );

        when(userRepository.findBySocialProviderAndSocialId("KAKAO", "no-role")).thenReturn(Optional.empty());
        when(userRepository.findByUserEmail("missing@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUserNickname(anyString())).thenReturn(Optional.empty());
        when(userRoleRepository.findFirstByUserRoleNameOrderByUserRoleIdAsc("USER"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(socialOAuth2UserService, "syncUserWith", profile))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("기본 USER 권한이 존재하지 않습니다.");

        verify(userRepository, never()).save(any());
    }
}
