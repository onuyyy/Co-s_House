package com.bird.cos.service.user;

import com.bird.cos.domain.user.PointHistory;
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
public class PointTransactionService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public void usePointsInTransaction(Long userId, int amount, String description, String referenceId, String referenceType) {
        User user = userRepository.findById(userId)
                .orElseThrow(BusinessException::userNotFound);

        UserPoint userPoint = getOrCreateUserPoint(userId);
        int currentPoints = userPoint.getAvailablePoint();

        if (currentPoints < amount) {
            throw BusinessException.pointInsufficient(userId, amount, currentPoints);
        }

        userPoint.usePoints(amount);

        PointHistory history = PointHistory.createUse(
                user, amount, currentPoints, userPoint.getAvailablePoint(),
                description, referenceId, referenceType
        );
        pointHistoryRepository.save(history);

        log.info("포인트 사용 완료 - userId: {}, amount: {}, balanceAfter: {}",
                userId, amount, userPoint.getAvailablePoint());
    }

    private UserPoint getOrCreateUserPoint(Long userId) {
        return userPointRepository.findByUser_UserId(userId)
                .orElseGet(() -> createUserPointEntity(userId));
    }

    private UserPoint createUserPointEntity(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(BusinessException::userNotFound);

        UserPoint newPoint = UserPoint.createForUser(user);
        return userPointRepository.save(newPoint);
    }
}
