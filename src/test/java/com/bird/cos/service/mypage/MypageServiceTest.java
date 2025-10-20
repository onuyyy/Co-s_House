package com.bird.cos.service.mypage;

import com.bird.cos.domain.user.User;
import com.bird.cos.dto.mypage.MypageUserUpdateRequest;
import com.bird.cos.repository.log.UserActivityLogRepository;
import com.bird.cos.repository.mypage.MypageRepository;
import com.bird.cos.repository.order.OrderRepository;
import com.bird.cos.repository.product.ReviewRepository;
import com.bird.cos.repository.question.QuestionRepository;
import com.bird.cos.repository.user.PointRepository;
import com.bird.cos.repository.user.UserGradeRepository;
import com.bird.cos.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MypageServiceTest {

    @Mock
    private MypageRepository mypageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserActivityLogRepository userActivityLogRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private UserGradeRepository userGradeRepository;

    @InjectMocks
    private MypageService mypageService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .userEmail("test@test.com")
                .userNickname("tester")
                .userName("테스터")
                .userPassword("encodedPassword")
                .userPhone("010-1234-5678")
                .userAddress("서울시 강남구, 상세주소")
                .socialProvider(null)
                .build();
    }

    @Test
    void updateUserInfoById_닉네임수정_성공() {
        // Given
        MypageUserUpdateRequest request = new MypageUserUpdateRequest();
        request.setUserNickname("새닉네임");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        mypageService.updateUserInfoById(1L, request, null);

        // Then
        verify(userRepository).save(user);
        assertThat(user.getUserNickname()).isEqualTo("새닉네임");
    }

    @Test
    void updateUserInfoById_전화번호수정_성공() {
        // Given
        MypageUserUpdateRequest request = new MypageUserUpdateRequest();
        request.setUserPhone("010-9999-8888");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        mypageService.updateUserInfoById(1L, request, null);

        // Then
        verify(userRepository).save(user);
        assertThat(user.getUserPhone()).isEqualTo("010-9999-8888");
    }

    @Test
    void updateUserInfoById_주소수정_성공() {
        // Given
        MypageUserUpdateRequest request = new MypageUserUpdateRequest();
        request.setUserAddress("부산시 해운대구");
        request.setUserDetailAddress("아파트 101동");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        mypageService.updateUserInfoById(1L, request, null);

        // Then
        verify(userRepository).save(user);
        assertThat(user.getUserAddress()).isEqualTo("부산시 해운대구, 아파트 101동");
    }

    @Test
    void updateUserInfoById_비밀번호수정_성공() {
        // Given
        MypageUserUpdateRequest request = new MypageUserUpdateRequest();
        request.setUserPassword("newPassword123");

        String currentPassword = "oldPassword123";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("newPassword123", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        mypageService.updateUserInfoById(1L, request, currentPassword);

        // Then
        verify(userRepository).save(user);
        verify(passwordEncoder).encode("newPassword123");
        assertThat(user.getUserPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    void updateUserInfoById_비밀번호수정시_현재비밀번호미입력_예외발생() {
        // Given
        MypageUserUpdateRequest request = new MypageUserUpdateRequest();
        request.setUserPassword("newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> mypageService.updateUserInfoById(1L, request, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 비밀번호를 입력해주세요.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserInfoById_비밀번호수정시_현재비밀번호불일치_예외발생() {
        // Given
        MypageUserUpdateRequest request = new MypageUserUpdateRequest();
        request.setUserPassword("newPassword123");

        String wrongPassword = "wrongPassword";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(wrongPassword, "encodedPassword")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> mypageService.updateUserInfoById(1L, request, wrongPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 비밀번호가 일치하지 않습니다.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserInfoById_새비밀번호가_현재비밀번호와동일_예외발생() {
        // Given
        MypageUserUpdateRequest request = new MypageUserUpdateRequest();
        request.setUserPassword("samePassword");

        String currentPassword = "samePassword";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("samePassword", "encodedPassword")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> mypageService.updateUserInfoById(1L, request, currentPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새 비밀번호는 현재 비밀번호와 달라야 합니다.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserInfoById_소셜로그인사용자_비밀번호변경시도_예외발생() {
        // Given
        User socialUser = User.builder()
                .userId(1L)
                .userEmail("kakao@test.com")
                .userNickname("카카오유저")
                .userName("카카오")
                .socialProvider("kakao")
                .socialId("kakao123")
                .build();

        MypageUserUpdateRequest request = new MypageUserUpdateRequest();
        request.setUserPassword("newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(socialUser));

        // When & Then
        assertThatThrownBy(() -> mypageService.updateUserInfoById(1L, request, "anyPassword"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserInfoById_존재하지않는사용자_예외발생() {
        // Given
        MypageUserUpdateRequest request = new MypageUserUpdateRequest();
        request.setUserNickname("새닉네임");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> mypageService.updateUserInfoById(999L, request, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserInfoById_회원탈퇴_성공() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.existsByUser_UserId(1L)).thenReturn(false);

        // When
        mypageService.deleteUserInfoById(1L, "서비스 불만족");

        // Then
        verify(questionRepository).anonymizeQuestionsByUser(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserInfoById_주문내역있음_예외발생() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.existsByUser_UserId(1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> mypageService.deleteUserInfoById(1L, "탈퇴 사유"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("진행 중이거나 완료된 주문 내역이 있어 탈퇴할 수 없습니다.");

        verify(userRepository, never()).delete(any());
    }

    @Test
    void requiresPassword_일반사용자_true반환() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        boolean result = mypageService.requiresPassword(1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void requiresPassword_소셜로그인사용자_false반환() {
        // Given
        User socialUser = User.builder()
                .userId(1L)
                .socialProvider("kakao")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(socialUser));

        // When
        boolean result = mypageService.requiresPassword(1L);

        // Then
        assertThat(result).isFalse();
    }
}
