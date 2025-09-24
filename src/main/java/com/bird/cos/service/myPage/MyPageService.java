package com.bird.cos.service.myPage;

import com.bird.cos.domain.user.User;
import com.bird.cos.dto.myPage.MyPageUserManageResponse;
import com.bird.cos.dto.myPage.MyPageUserUpdateRequest;
import com.bird.cos.repository.myPage.MyPageRepository;
import com.bird.cos.repository.question.QuestionRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MyPageService {

    private final MyPageRepository myPageRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuestionRepository questionRepository;

    //UserId 정보 넘기기
    public MyPageUserManageResponse getUserInfoById(Long userId){
        User user = myPageRepository.findUserForMyPage(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return MyPageUserManageResponse.from(user);
    }

    //회원 정보 수정
    @Transactional
    public void updateUserInfoById(Long userId, MyPageUserUpdateRequest request, String currentPassword){
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 변경 시 현재 비밀번호 검증
        if (request.getUserPassword() != null && !request.getUserPassword().isEmpty()) {
            // 소셜 로그인 사용자는 비밀번호 변경 불가
            if (existingUser.getSocialProvider() != null) {
                throw new IllegalStateException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
            }

            // 현재 비밀번호가 입력되지 않았거나 일치하지 않는 경우
            if (currentPassword == null || currentPassword.isEmpty()) {
                throw new IllegalArgumentException("현재 비밀번호를 입력해주세요.");
            }

            if (!passwordEncoder.matches(currentPassword, existingUser.getUserPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            // 새 비밀번호가 현재 비밀번호와 같은지 확인
            if (passwordEncoder.matches(request.getUserPassword(), existingUser.getUserPassword())) {
                throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
            }
        }

        User updateUser = User.builder()
                .userId(existingUser.getUserId())                    // 기존 ID 유지
                .userName(existingUser.getUserName())                // 기존 이름 유지 (변경불가)
                .userEmail(existingUser.getUserEmail())              // 기존 이메일 유지 (변경불가)
                .userPassword(request.getUserPassword() != null && !request.getUserPassword().isEmpty()
                        ? passwordEncoder.encode(request.getUserPassword())  // 새 비밀번호 암호화
                        : existingUser.getUserPassword())                 // 기존 비밀번호 유지
                .userNickname(request.getUserNickname())             // 새 닉네임으로 변경
                .userPhone(request.getUserPhone())                   // 새 전화번호로 변경
                .userAddress(request.getUserAddress())               // 새 주소로 변경
                .userRole(existingUser.getUserRole())                // 기존 권한 유지
                .socialProvider(existingUser.getSocialProvider())    // 기존 소셜로그인 정보 유지
                .userCreatedAt(existingUser.getUserCreatedAt())      // 기존 가입일 유지
                .build();

        userRepository.save(updateUser);
    }

    //회원 탈퇴
    @Transactional
    public void deleteUserInfoById(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 1. 문의글 익명화 (사용자 연결 해제)
        questionRepository.anonymizeQuestionsByUser(userId);

        // 2. 주문내역은 유지 (이 부분은 주문쪽 끝나면 구현될 예정 9.22)
        // orderRepository.anonymizeOrdersByUser(user);

        // 3. 사용자 삭제
        userRepository.delete(user);
    }

    // Authentication에서 userId 추출
    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        String userEmail = authentication.getName();
        if (userEmail == null) {
            throw new IllegalArgumentException("사용자 이메일 정보가 없습니다.");
        }

        User user = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return user.getUserId();
    }

    // 비밀번호 검증
    public boolean validateCurrentPassword(String currentPassword, Authentication authentication, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 소셜 로그인 사용자는 비밀번호가 없음
            if (user.getSocialProvider() != null) {
                return false;
            }

            boolean isValid = passwordEncoder.matches(currentPassword, user.getUserPassword());

            // 보안 로깅
            String clientIP = getClientIP(request);
            if (!isValid) {
                log.warn("사용자 비밀번호 검증 실패: 사용자 ID: {}, IP: {}", userId, clientIP);
            }

            return isValid;
        } catch (Exception e) {
            // 에러 정보 숨기기
            return false;
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}



