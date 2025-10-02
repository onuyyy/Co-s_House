package com.bird.cos.service.mypage;

import com.bird.cos.domain.product.Review;
import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserGrade;
import com.bird.cos.dto.order.OrderStatusCode;
import com.bird.cos.dto.mypage.MypageUserManageResponse;
import com.bird.cos.dto.mypage.MypageUserUpdateRequest;
import com.bird.cos.dto.product.ReviewResponse;
import com.bird.cos.repository.log.UserActivityLogRepository;
import com.bird.cos.repository.mypage.MypageRepository;
import com.bird.cos.repository.order.OrderRepository;
import com.bird.cos.repository.product.ReviewRepository;
import com.bird.cos.repository.question.QuestionRepository;
import com.bird.cos.repository.user.PointRepository;
import com.bird.cos.repository.user.UserGradeRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MypageService {

    private static final List<String> ORDER_SUMMARY_STATUS_CODES = List.of(
            OrderStatusCode.DELIVERED.getCode(),
            OrderStatusCode.CONFIRMED.getCode()
    );

    private final MypageRepository myPageRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuestionRepository questionRepository;
    private final UserActivityLogRepository userActivityLogRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final PointRepository pointRepository;
    private final UserGradeRepository userGradeRepository;

    //UserId 정보 넘기기
    public MypageUserManageResponse getUserInfoById(Long userId){
        User user = myPageRepository.findUserForMyPage(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        AddressParts addressParts = splitAddress(user.getUserAddress());

        Integer membershipPoints = Optional.ofNullable(pointRepository.getTotalPointsByUserId(userId)).orElse(0);
        BigDecimal totalOrderAmount = Optional.ofNullable(
                orderRepository.sumOrderAmountByStatusCodes(userId, ORDER_SUMMARY_STATUS_CODES)
        ).orElse(BigDecimal.ZERO);

        Optional<UserGrade> latestGrade = userGradeRepository.findTopByUser_UserIdOrderByGradePeriodEndDesc(userId);
        Integer gradeLevel = latestGrade.map(UserGrade::getGradeLevel).orElse(null);
        String gradeName = resolveMembershipGradeName(gradeLevel);

        return MypageUserManageResponse.builder()
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .userPhone(user.getUserPhone())
                .userAddress(addressParts.base())
                .userDetailAddress(addressParts.detail())
                .userNickname(user.getUserNickname())
                .userCreatedAt(user.getUserCreatedAt())
                .socialProvider(user.getSocialProvider())
                .userRole(user.getUserRole() != null ? user.getUserRole().getUserRoleName() : null)
                .membershipGrade(gradeLevel)
                .membershipGradeName(gradeName)
                .totalOrderAmount(totalOrderAmount)
                .membershipPoints(membershipPoints)
                .build();
    }

    //회원 정보 수정
    @Transactional
    public void updateUserInfoById(Long userId, MypageUserUpdateRequest request, String currentPassword){
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 변경 시 현재 비밀번호 검증
        if (StringUtils.hasText(request.getUserPassword())) {
            // 소셜 로그인 사용자는 비밀번호 변경 불가
            if (existingUser.getSocialProvider() != null) {
                throw new IllegalStateException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
            }

            // 현재 비밀번호가 입력되지 않았거나 일치하지 않는 경우
            if (!StringUtils.hasText(currentPassword)) {
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

        AddressParts existingAddressParts = splitAddress(existingUser.getUserAddress());

        String nickname = StringUtils.hasText(request.getUserNickname())
                ? request.getUserNickname().trim()
                : existingUser.getUserNickname();

        String phone = StringUtils.hasText(request.getUserPhone())
                ? request.getUserPhone().trim()
                : existingUser.getUserPhone();

        String baseAddress = StringUtils.hasText(request.getUserAddress())
                ? request.getUserAddress().trim()
                : existingAddressParts.base();

        String detailAddress = StringUtils.hasText(request.getUserDetailAddress())
                ? request.getUserDetailAddress().trim()
                : existingAddressParts.detail();

        String combinedAddress = combineAddress(baseAddress, detailAddress);

        if (StringUtils.hasText(request.getUserPassword())) {
            existingUser.updatePassword(passwordEncoder.encode(request.getUserPassword()));
        }

        existingUser.updateNickname(nickname);
        existingUser.updatePhone(phone);
        existingUser.updateAddress(combinedAddress);

        userRepository.save(existingUser);
    }

    //회원 탈퇴
    @Transactional
    public void deleteUserInfoById(Long userId, String withdrawalReason){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (orderRepository.existsByUser_UserId(userId)) {
            throw new IllegalStateException("진행 중이거나 완료된 주문 내역이 있어 탈퇴할 수 없습니다.");
        }

        // 1. 사용자 활동 로그 삭제 (외래키 제약조건으로 인해 먼저 삭제)
        userActivityLogRepository.deleteByUserId(user);

        // 2. 문의글 익명화 (사용자 연결 해제)
        questionRepository.anonymizeQuestionsByUser(userId);

        // 3. 주문내역은 유지 (이 부분은 주문쪽 끝나면 구현될 예정 9.22)
        // orderRepository.anonymizeOrdersByUser(user);

        // 4. 사용자 삭제
        log.info("회원 탈퇴 - userId: {}, reason: {}", userId, withdrawalReason);
        userRepository.delete(user);
    }

    public boolean requiresPassword(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getSocialProvider() == null)
                .orElse(false);
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

    private String combineAddress(String baseAddress, String detailAddress) {
        if (baseAddress == null || baseAddress.trim().isEmpty()) {
            return detailAddress != null ? detailAddress.trim() : null;
        }

        if (detailAddress == null || detailAddress.trim().isEmpty()) {
            return baseAddress.trim();
        }

        return baseAddress.trim() + ", " + detailAddress.trim();
    }

    private AddressParts splitAddress(String combinedAddress) {
        if (!StringUtils.hasText(combinedAddress)) {
            return new AddressParts(null, null);
        }

        String[] parts = combinedAddress.split(",", 2);
        String base = parts[0].trim();
        String detail = parts.length > 1 ? parts[1].trim() : null;

        return new AddressParts(base, detail);
    }

    private String resolveMembershipGradeName(Integer gradeLevel) {
        if (gradeLevel == null) {
            return null;
        }

        return switch (gradeLevel) {
            case 1 -> "브론즈";
            case 2 -> "실버";
            case 3 -> "골드";
            case 4 -> "플래티넘";
            default -> null;
        };
    }

    private record AddressParts(String base, String detail) {}

    public Map<String, Object> getMyReviews(String userNickname, String sort, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, getReviewSort(sort));
        Page<Review> reviewPage = reviewRepository.findByUserNickname(userNickname, pageable);

        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(ReviewResponse::fromEntity)  // from -> fromEntity로 변경
                .collect(Collectors.toList());

        return Map.of(
                "reviews", reviews,
                "totalPages", reviewPage.getTotalPages(),
                "totalElements", reviewPage.getTotalElements()
        );
    }

    private Sort getReviewSort(String sort) {
        return switch (sort) {
            case "oldest" -> Sort.by("createdAt").ascending();
            case "rating-high" -> Sort.by("rating").descending();
            case "rating-low" -> Sort.by("rating").ascending();
            default -> Sort.by("createdAt").descending(); // latest
        };
    }
}
