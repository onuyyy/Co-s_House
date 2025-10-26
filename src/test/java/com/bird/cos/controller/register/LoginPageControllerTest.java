package com.bird.cos.controller.register;

import com.bird.cos.domain.user.User;
import com.bird.cos.repository.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * LoginPageController의 로그인/로그아웃 웹 흐름을 검증.
 * AuthenticationManager 결과에 따라 SecurityContext, 세션, 리다이렉트가 기대대로 동작하는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class LoginPageControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoginPageController loginPageController;

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginForm_WithValidCredentials_RedirectsHomeAndStoresSession() {
        // 인증이 성공하면 SecurityContext와 세션이 업데이트되고 홈으로 리다이렉트되는지 검증
        String loginEmail = "user@example.com";

        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setSession(session);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        List<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("user_role"),
                new SimpleGrantedAuthority("admin_role")
        );
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        User user = User.builder()
                .userId(1L)
                .userEmail(loginEmail)
                .userName("테스트")
                .userNickname("tester")
                .build();
        when(userRepository.findByUserEmail(loginEmail)).thenReturn(Optional.of(user));

        ArgumentCaptor<SecurityContext> contextCaptor = ArgumentCaptor.forClass(SecurityContext.class);
        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        String view = loginPageController.loginForm(loginEmail, "password", session, request, response);

        assertThat(view).isEqualTo("redirect:/api/admin");
        assertThat(session.getAttribute("userId")).isEqualTo(1L);
        assertThat(session.getAttribute("userEmail")).isEqualTo(loginEmail);
        assertThat(session.getAttribute("userName")).isEqualTo("테스트");
        assertThat(session.getAttribute("user")).isEqualTo(user);

        verify(authenticationManager).authenticate(tokenCaptor.capture());
        UsernamePasswordAuthenticationToken token = tokenCaptor.getValue();
        assertThat(token.getName()).isEqualTo(loginEmail);
        assertThat(token.getCredentials()).isEqualTo("password");

        verify(userRepository).findByUserEmail(loginEmail);
        verify(securityContextRepository).saveContext(contextCaptor.capture(), eq(request), eq(response));
        assertThat(contextCaptor.getValue().getAuthentication()).isEqualTo(authentication);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
    }

    @Test
    void loginForm_WithInvalidPassword_RedirectsWithError() {
        // 비밀번호 오류 시 BadCredentialsException을 잡고 에러 파라미터로 리다이렉트하는지 확인
        String loginEmail = "user@example.com";

        HttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setSession(session);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        String view = loginPageController.loginForm(loginEmail, "wrongPassword", session, request, response);

        assertThat(view).isEqualTo("redirect:/controller/register/login?error=password");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userRepository);
        verify(securityContextRepository, never()).saveContext(any(), any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void logout_InvalidatesSessionAndRedirectsHome() {
        // 로그아웃 실행 시 세션이 무효화되고 루트로 리다이렉트하는지 검증
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userEmail", "user@example.com");

        String view = loginPageController.logout(session);

        assertThat(view).isEqualTo("redirect:/");
        assertThatThrownBy(() -> session.getAttribute("userEmail"))
                .isInstanceOf(IllegalStateException.class);
    }
}
