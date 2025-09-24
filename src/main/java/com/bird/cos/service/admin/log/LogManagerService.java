package com.bird.cos.service.admin.log;

import com.bird.cos.domain.log.UserActivityLog;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.admin.LogResponse;
import com.bird.cos.repository.log.UserActivityLogRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class LogManagerService {

    private final UserActivityLogRepository userActivityLogRepository;
    private final UserRepository userRepository;

    public Page<LogResponse> getLogList(LocalDate startDate, LocalDate endDate, String username, Pageable pageable) {

        LocalDate start = (startDate == null) ? LocalDate.now().minusMonths(1) : startDate;
        LocalDate end = (endDate == null) ? LocalDate.now() : endDate;

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);

        Page<UserActivityLog> userActivityLogs;

        if (username == null || username.trim().isEmpty()) {
            userActivityLogs = userActivityLogRepository.findByActivityTimeBetween(startDateTime, endDateTime, pageable);
        } else {
            userActivityLogs = userActivityLogRepository.findByUserId_userNameContainingAndActivityTimeBetween(username.trim(), startDateTime, endDateTime, pageable);
        }

        return userActivityLogs.map(LogResponse::from);
    }
}
