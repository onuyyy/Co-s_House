package com.bird.cos.service.admin;

import com.bird.cos.domain.log.UserActivityLog;
import com.bird.cos.dto.admin.AdminAccessLogDto;
import com.bird.cos.repository.log.UserActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLogService {
    
    private final UserActivityLogRepository userActivityLogRepository;
    
    /**
     * 모든 관리자 페이지 접근 로그 조회
     */
    public Page<AdminAccessLogDto> getAllAdminAccessLogs(Pageable pageable) {
        Page<UserActivityLog> logs = userActivityLogRepository
                .findByIsAdminAccessTrueOrderByActivityTimeDesc(pageable);
        return logs.map(AdminAccessLogDto::from);
    }
    
    /**
     * 접근 결과별 관리자 로그 조회
     */
    public Page<AdminAccessLogDto> getAdminAccessLogsByResult(
            UserActivityLog.AccessResult accessResult, Pageable pageable) {
        Page<UserActivityLog> logs = userActivityLogRepository
                .findByIsAdminAccessTrueAndAccessResultOrderByActivityTimeDesc(accessResult, pageable);
        return logs.map(AdminAccessLogDto::from);
    }
    
    /**
     * 실패한 관리자 접근 시도 조회
     */
    public Page<AdminAccessLogDto> getFailedAdminAccess(Pageable pageable) {
        Page<UserActivityLog> logs = userActivityLogRepository.findFailedAdminAccess(pageable);
        return logs.map(AdminAccessLogDto::from);
    }
    
    /**
     * 특정 기간의 관리자 접근 로그 조회
     */
    public Page<AdminAccessLogDto> getAdminAccessLogsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<UserActivityLog> logs = userActivityLogRepository
                .findByIsAdminAccessTrueAndActivityTimeBetweenOrderByActivityTimeDesc(
                        startDate, endDate, pageable);
        return logs.map(AdminAccessLogDto::from);
    }
    
    /**
     * 사용자명으로 관리자 접근 로그 검색
     */
    public Page<AdminAccessLogDto> searchAdminAccessByUserName(String userName, Pageable pageable) {
        Page<UserActivityLog> logs = userActivityLogRepository
                .findAdminAccessByUserName(userName, pageable);
        return logs.map(AdminAccessLogDto::from);
    }
    
    /**
     * 관리자 접근 통계
     */
    public Map<String, Object> getAdminAccessStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 전체 관리자 접근 수
        long totalAccess = userActivityLogRepository.findAll().stream()
                .filter(log -> Boolean.TRUE.equals(log.getIsAdminAccess()))
                .count();
        
        // 성공/실패 비율
        long successCount = userActivityLogRepository.findAll().stream()
                .filter(log -> Boolean.TRUE.equals(log.getIsAdminAccess()))
                .filter(log -> UserActivityLog.AccessResult.SUCCESS.equals(log.getAccessResult()))
                .count();
        
        long failedCount = userActivityLogRepository.findAll().stream()
                .filter(log -> Boolean.TRUE.equals(log.getIsAdminAccess()))
                .filter(log -> !UserActivityLog.AccessResult.SUCCESS.equals(log.getAccessResult()))
                .count();
        
        stats.put("totalAccess", totalAccess);
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("successRate", totalAccess > 0 ? (successCount * 100.0 / totalAccess) : 0);
        
        return stats;
    }
}
