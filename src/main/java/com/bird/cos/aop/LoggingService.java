package com.bird.cos.aop;

import com.bird.cos.domain.log.UserActivityLog;
import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserRole;
import com.bird.cos.repository.log.UserActivityLogRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoggingService {

    private final UserActivityLogRepository userActivityLogRepository;
    private final UserRepository userRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSave(UserActivityRequest request) {
        try {
            User user = null;
            UserRole userRole = null;

            if (!"anonymous".equals(request.getUsername())) {
                Optional<User> userOpt = userRepository.findByUserEmail(request.getUsername());
                if (userOpt.isPresent()) {
                    user = userOpt.get();

                    if (user.getUserRole() != null) {
                        userRole = user.getUserRole();
                    }
                }
            }

            //  UserActivityLog 엔티티 생성 및 저장
            UserActivityLog activityLog = UserActivityLog.builder()
                    .userId(user)
                    .userRole(userRole)
                    .pageUrl(request.getPageUrl())
                    .referrerUrl(request.getReferrerUrl())
                    .activityTime(request.getActivityTime())
                    .userAgent(request.getUserAgent())
                    .ipAddress(request.getIpAddress())
                    .sessionId(request.getSessionId())
                    .targetId(request.getTargetId())
                    .build();

            userActivityLogRepository.save(activityLog);

            log.info("Activity log saved - User: {}, Role: {}, URL: {}",
                    request.getUsername(),
                    userRole != null ? userRole.getUserRoleName() : "UNKNOWN",
                    request.getPageUrl());

        } catch (Exception e) {
            // todo : 예외 처리
        }
    }
}
