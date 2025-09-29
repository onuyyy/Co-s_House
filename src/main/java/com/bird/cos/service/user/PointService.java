package com.bird.cos.service.user;

import com.bird.cos.domain.user.PointHistory;
import com.bird.cos.domain.user.PointType;
import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserPoint;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.user.PointHistoryRepository;
import com.bird.cos.repository.user.UserPointRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 현재 사용 가능한 포인트 조회
     * @param userId 사용자 ID
     * @return 사용 가능한 포인트
     */
    public Integer getAvailablePoints(Long userId) {
        return userPointRepository.getAvailablePointByUserId(userId);
    }

    /**
     * 사용자의 포인트 엔티티 조회 (없으면 새로 생성)
     * @param userId 사용자 ID
     * @return UserPoint 엔티티
     */
    @Transactional
    public UserPoint getOrCreateUserPoint(Long userId) {
        return userPointRepository.findByUser_UserId(userId)
                .orElseGet(() -> createUserPointEntity(userId));
    }

    /**
     * 사용자 포인트 엔티티 생성
     */
    private UserPoint createUserPointEntity(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(BusinessException::userNotFound);

        UserPoint newPoint = UserPoint.createForUser(user);
        return userPointRepository.save(newPoint);
    }

    /**
     * 포인트 적립
     * @param userId 사용자 ID
     * @param amount 적립할 포인트 (양수)
     * @param description 적립 사유
     * @param referenceId 참조 ID
     * @param referenceType 참조 타입
     */
    @Transactional
    public void earnPoints(Long userId, int amount, String description, String referenceId, String referenceType) {
        validatePositiveAmount(amount);

        UserPoint point = getOrCreateUserPoint(userId);
        int balanceBefore = point.getAvailablePoint();

        // 포인트 적립
        point.earnPoints(amount);
        userPointRepository.save(point);

        // 내역 저장
        PointHistory history = PointHistory.createEarn(
                point.getUser(), amount, balanceBefore, point.getAvailablePoint(),
                description, referenceId, referenceType
        );
        pointHistoryRepository.save(history);
    }

    /**
     * 포인트 사용
     * @param userId 사용자 ID
     * @param amount 사용할 포인트 (양수)
     * @param description 사용 사유
     * @param referenceId 참조 ID
     * @param referenceType 참조 타입
     * @throws BusinessException 사용 가능한 포인트가 부족한 경우
     */
    @Transactional
    public void usePoints(Long userId, int amount, String description, String referenceId, String referenceType) {
        validatePositiveAmount(amount);

        UserPoint point = getOrCreateUserPoint(userId);
        int balanceBefore = point.getAvailablePoint();

        // 포인트 사용 (UserPoint 엔티티에서 잔액 부족 검증)
        point.usePoints(amount);
        userPointRepository.save(point);

        // 내역 저장
        PointHistory history = PointHistory.createUse(
                point.getUser(), amount, balanceBefore, point.getAvailablePoint(),
                description, referenceId, referenceType
        );
        pointHistoryRepository.save(history);

        log.info("포인트 사용 완료 - userId: {}, amount: {}, balanceAfter: {}",
                userId, amount, point.getAvailablePoint());
    }


    /**
     * 포인트 사용 가능 여부 검증
     * @param userId 사용자 ID
     * @param requestAmount 사용하려는 포인트
     * @return 사용 가능 여부
     */
    public boolean canUsePoints(Long userId, int requestAmount) {
        if (requestAmount <= 0) {
            return false;
        }

        UserPoint point = userPointRepository.findByUser_UserId(userId).orElse(null);
        return point != null && point.canUse(requestAmount);
    }

    /**
     * 사용 가능한 최대 포인트 조회
     * @param userId 사용자 ID
     * @param orderAmount 주문 금액 (포인트는 주문 금액을 초과할 수 없음)
     * @return 사용 가능한 최대 포인트
     */
    public Integer getMaxUsablePoints(Long userId, Integer orderAmount) {
        Integer availablePoints = getAvailablePoints(userId);

        if (orderAmount == null || orderAmount <= 0) {
            return availablePoints;
        }

        // 포인트는 주문 금액을 초과하여 사용할 수 없음
        return Math.min(availablePoints, orderAmount);
    }

    /**
     * 중복 포인트 적립 방지 검증
     * @param referenceId 참조 ID
     * @param referenceType 참조 타입
     * @param type 포인트 타입
     * @return 중복 여부
     */
    public boolean isDuplicatePointTransaction(String referenceId, String referenceType, PointType type) {
        return pointHistoryRepository.existsByReferenceIdAndReferenceTypeAndType(referenceId, referenceType, type);
    }

    /**
     * 주문 적립 포인트 계산 및 적립
     * @param userId 사용자 ID
     * @param orderAmount 주문 금액
     * @param orderId 주문 ID
     * @param earnRate 적립율 (%)
     */
    @Transactional
    public void earnOrderPoints(Long userId, int orderAmount, String orderId, double earnRate) {
        // 중복 적립 방지
        if (isDuplicatePointTransaction(orderId, "ORDER", PointType.EARN)) {
            log.warn("중복 포인트 적립 시도 - userId: {}, orderId: {}", userId, orderId);
            return;
        }

        int earnAmount = (int) (orderAmount * earnRate / 100);
        if (earnAmount > 0) {
            earnPoints(userId, earnAmount,
                    String.format("주문 적립 (%.1f%%)", earnRate),
                    orderId, "ORDER");
        }
    }

    /**
     * 주문 포인트 사용
     * @param userId 사용자 ID
     * @param useAmount 사용할 포인트
     * @param orderId 주문 ID
     */
    @Transactional
    public void useOrderPoints(Long userId, int useAmount, String orderId) {
        if (useAmount > 0) {
            usePoints(userId, useAmount, "주문 결제", orderId, "ORDER");
        }
    }

    /**
     * 포인트 금액 양수 검증
     */
    private void validatePositiveAmount(int amount) {
        if (amount <= 0) {
            throw BusinessException.pointInvalidAmount(amount);
        }
    }
}