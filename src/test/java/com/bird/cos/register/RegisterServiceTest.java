package com.bird.cos.register;

import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserRole;
import com.bird.cos.dto.user.RegisterRequest;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.repository.user.UserRoleRepository;
import com.bird.cos.service.auth.EmailVerificationService;
import com.bird.cos.service.register.RegisterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private RegisterService registerService;

    @Test
    void register_WithValidRequest_SavesUserAndConsumesVerification() {
        // 정상 회원가입 시 이메일 정규화, 비밀번호 암호화, 기본 역할 할당, 인증 소비까지 이루어지는지 검증
        RegisterRequest request = new RegisterRequest(
                "홍길동",
                "길동이",
                " Test@Example.com ",
                "plainPassword",
                "010-1234-5678",
                "서울시 중구 어딘가"
        );

        UserRole defaultRole = mock(UserRole.class);

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUserNickname("길동이")).thenReturn(Optional.empty());
        when(emailVerificationService.isVerified("test@example.com")).thenReturn(true);
        when(userRoleRepository.findById(1L)).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        User result = registerService.register(request);

        User saved = userCaptor.getValue();
        assertThat(saved.getUserEmail()).isEqualTo("test@example.com");
        assertThat(saved.getUserPassword()).isEqualTo("encodedPassword");
        assertThat(saved.getUserRole()).isEqualTo(defaultRole);
        assertThat(saved.getUserNickname()).isEqualTo("길동이");
        assertThat(saved.getUserAddress()).isEqualTo("서울시 중구 어딘가");
        assertThat(saved.getUserPhone()).isEqualTo("010-1234-5678");
        assertThat(saved.isEmailVerified()).isTrue();
        assertThat(saved.getTermsAgreed()).isTrue();
        assertThat(result).isSameAs(saved);

        verify(emailVerificationService).consumeVerification("test@example.com");
    }

    @Test
    void register_WithDuplicatedEmail_ThrowsIllegalArgumentException() {
        // 이메일이 이미 존재하면 닉네임/인증 로직 실행 없이 예외가 발생해야 한다
        RegisterRequest request = new RegisterRequest(
                "홍길동",
                "길동이",
                "duplicate@example.com",
                "plainPassword",
                "010-2222-3333",
                "서울시"
        );

        when(userRepository.findByUserEmail("duplicate@example.com"))
                .thenReturn(Optional.of(User.builder().userEmail("duplicate@example.com").build()));

        assertThatThrownBy(() -> registerService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

        verify(userRepository, never()).findByUserNickname(anyString());
        verify(emailVerificationService, never()).isVerified(anyString());
        verify(emailVerificationService, never()).consumeVerification(anyString());
    }

    @Test
    void register_WithDuplicatedNickname_ThrowsIllegalArgumentException() {
        // 닉네임이 중복된 경우 이메일 인증 여부를 확인하기 전에 예외가 발생해야 한다
        RegisterRequest request = new RegisterRequest(
                "홍길동",
                "중복닉",
                "unique@example.com",
                "plainPassword",
                "010-4444-5555",
                "서울시"
        );

        when(userRepository.findByUserEmail("unique@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUserNickname("중복닉"))
                .thenReturn(Optional.of(User.builder().userNickname("중복닉").build()));

        assertThatThrownBy(() -> registerService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");

        verify(emailVerificationService, never()).isVerified(anyString());
        verify(emailVerificationService, never()).consumeVerification(anyString());
    }

    @Test
    void register_WhenEmailNotVerified_ThrowsIllegalStateException() {
        // 이메일 인증이 완료되지 않았다면 기본 역할 조회 및 저장이 이루어지지 않아야 한다
        RegisterRequest request = new RegisterRequest(
                "홍길동",
                "길동이",
                "notverified@example.com",
                "plainPassword",
                "010-6666-7777",
                "서울시"
        );

        when(userRepository.findByUserEmail("notverified@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUserNickname("길동이")).thenReturn(Optional.empty());
        when(emailVerificationService.isVerified("notverified@example.com")).thenReturn(false);

        assertThatThrownBy(() -> registerService.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이메일 인증을 완료해주세요.");

        verify(userRoleRepository, never()).findById(anyLong());
        verify(emailVerificationService, never()).consumeVerification(anyString());
    }

    @Test
    void register_WhenDefaultRoleMissing_ThrowsIllegalStateException() {
        // 기본 역할이 없으면 저장에 실패하고 이메일 인증 소비도 진행되지 않아야 한다
        RegisterRequest request = new RegisterRequest(
                "홍길동",
                "길동이",
                "rolemissing@example.com",
                "plainPassword",
                "010-8888-9999",
                "서울시"
        );

        when(userRepository.findByUserEmail("rolemissing@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUserNickname("길동이")).thenReturn(Optional.empty());
        when(emailVerificationService.isVerified("rolemissing@example.com")).thenReturn(true);
        when(userRoleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registerService.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("기본 역할(id=1)을 찾을 수 없습니다.");

        verify(emailVerificationService, never()).consumeVerification(anyString());
    }
}
