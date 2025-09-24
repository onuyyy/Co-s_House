package com.bird.cos.service.myPage;

import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserRole;
import com.bird.cos.dto.myPage.MyPageUserManageResponse;
import com.bird.cos.dto.myPage.MyPageUserUpdateRequest;
import com.bird.cos.repository.myPage.MyPageRepository;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.repository.question.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
@DisplayName("MyPageService 테스트")
class MyPageServiceTest {

    @Mock
    private MyPageRepository myPageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private MyPageService myPageService;

    private User testUser;
    private User socialUser;
    private MyPageUserUpdateRequest updateRequest;
    private UserRole testUserRole;

    @BeforeEach
    void setUp() {
        testUserRole = new UserRole();

        testUser = User.builder()
                .userId(1L)
                .userEmail("test@example.com")
                .userName("테스트사용자")
                .userNickname("테스트닉네임")
                .userPhone("010-1234-5678")
                .userAddress("서울시 강남구")
                .userPassword("encodedPassword")
                .userRole(testUserRole)
                .socialProvider(null)
                .userCreatedAt(LocalDateTime.now())
                .termsAgreed(true)
                .build();

        socialUser = User.builder()
                .userId(2L)
                .userEmail("social@example.com")
                .userName("소셜사용자")
                .userNickname("소셜닉네임")
                .socialProvider("google")
                .userCreatedAt(LocalDateTime.now())
                .termsAgreed(true)
                .build();

        updateRequest = new MyPageUserUpdateRequest();
        updateRequest.setUserNickname("수정된닉네임");
        updateRequest.setUserPhone("010-9876-5432");
        updateRequest.setUserAddress("서울시 서초구");
    }

    @Test
    @DisplayName("ID로 사용자 정보를 성공적으로 조회한다")
    void getUserInfoById_성공() {
        // Given
        given(myPageRepository.findUserForMyPage(1L)).willReturn(Optional.of(testUser));

        // When
        MyPageUserManageResponse result = myPageService.getUserInfoById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserEmail()).isEqualTo("test@example.com");
        assertThat(result.getUserName()).isEqualTo("테스트사용자");

        verify(myPageRepository, times(1)).findUserForMyPage(1L);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    void getUserInfoById_사용자없음_예외() {
        // Given
        given(myPageRepository.findUserForMyPage(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> myPageService.getUserInfoById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(myPageRepository, times(1)).findUserForMyPage(999L);
    }

    @Test
    @DisplayName("비밀번호 없이 사용자 정보를 성공적으로 업데이트한다")
    void updateUserInfoById_비밀번호없음_성공() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // When
        myPageService.updateUserInfoById(1L, updateRequest, null);

        // Then
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("올바른 현재 비밀번호로 비밀번호를 성공적으로 변경한다")
    void updateUserInfoById_비밀번호변경_성공() {
        // Given
        updateRequest.setUserPassword("newPassword");
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("currentPassword", "encodedPassword")).willReturn(true);
        given(passwordEncoder.matches("newPassword", "encodedPassword")).willReturn(false);
        given(passwordEncoder.encode("newPassword")).willReturn("newEncodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // When
        myPageService.updateUserInfoById(1L, updateRequest, "currentPassword");

        // Then
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("currentPassword", "encodedPassword");
        verify(passwordEncoder, times(1)).matches("newPassword", "encodedPassword");
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("소셜 로그인 사용자의 비밀번호 변경 시도 시 예외가 발생한다")
    void updateUserInfoById_소셜사용자_비밀번호변경_예외() {
        // Given
        updateRequest.setUserPassword("newPassword");
        given(userRepository.findById(2L)).willReturn(Optional.of(socialUser));

        // When & Then
        assertThatThrownBy(() -> myPageService.updateUserInfoById(2L, updateRequest, "currentPassword"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");

        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("현재 비밀번호가 일치하지 않을 때 예외가 발생한다")
    void updateUserInfoById_잘못된현재비밀번호_예외() {
        // Given
        updateRequest.setUserPassword("newPassword");
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> myPageService.updateUserInfoById(1L, updateRequest, "wrongPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 비밀번호가 일치하지 않습니다.");

        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("wrongPassword", "encodedPassword");
    }

    @Test
    @DisplayName("새 비밀번호가 현재 비밀번호와 같을 때 예외가 발생한다")
    void updateUserInfoById_동일한비밀번호_예외() {
        // Given
        updateRequest.setUserPassword("currentPassword");
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("currentPassword", "encodedPassword")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> myPageService.updateUserInfoById(1L, updateRequest, "currentPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새 비밀번호는 현재 비밀번호와 달라야 합니다.");

        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(2)).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("현재 비밀번호 없이 비밀번호 변경 시도 시 예외가 발생한다")
    void updateUserInfoById_현재비밀번호없음_예외() {
        // Given
        updateRequest.setUserPassword("newPassword");
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> myPageService.updateUserInfoById(1L, updateRequest, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 비밀번호를 입력해주세요.");

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("ID 기반으로 사용자를 성공적으로 삭제한다")
    void deleteUserInfoById_성공() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        doNothing().when(questionRepository).anonymizeQuestionsByUser(1L);
        doNothing().when(userRepository).delete(testUser);

        // When
        myPageService.deleteUserInfoById(1L);

        // Then
        verify(userRepository, times(1)).findById(1L);
        verify(questionRepository, times(1)).anonymizeQuestionsByUser(1L);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 삭제 시 예외가 발생한다")
    void deleteUserInfoById_사용자없음_예외() {
        // Given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> myPageService.deleteUserInfoById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository, times(1)).findById(999L);
    }
}