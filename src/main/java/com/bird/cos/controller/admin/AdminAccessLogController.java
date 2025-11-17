package com.bird.cos.controller.admin;

import com.bird.cos.domain.log.UserActivityLog;
import com.bird.cos.dto.admin.AdminAccessLogDto;
import com.bird.cos.service.admin.AdminLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/api/admin/access-logs")
@RequiredArgsConstructor
public class AdminAccessLogController {
    
    private final AdminLogService adminLogService;
    
    @GetMapping
    public String adminAccessLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminAccessLogDto> logs;
        
        // 필터 조건에 따른 조회
        if (userName != null && !userName.isEmpty()) {
            logs = adminLogService.searchAdminAccessByUserName(userName, pageable);
        } else if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            logs = adminLogService.getAdminAccessLogsByDateRange(startDateTime, endDateTime, pageable);
        } else if ("failed".equals(result)) {
            logs = adminLogService.getFailedAdminAccess(pageable);
        } else if (result != null && !result.isEmpty()) {
            try {
                UserActivityLog.AccessResult accessResult = UserActivityLog.AccessResult.valueOf(result.toUpperCase());
                logs = adminLogService.getAdminAccessLogsByResult(accessResult, pageable);
            } catch (IllegalArgumentException e) {
                logs = adminLogService.getAllAdminAccessLogs(pageable);
            }
        } else {
            logs = adminLogService.getAllAdminAccessLogs(pageable);
        }
        
        // 통계 정보
        Map<String, Object> stats = adminLogService.getAdminAccessStatistics();
        
        model.addAttribute("logs", logs);
        model.addAttribute("stats", stats);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentResult", result);
        model.addAttribute("currentUserName", userName);
        model.addAttribute("currentStartDate", startDate);
        model.addAttribute("currentEndDate", endDate);
        
        return "admin/access-logs";
    }
}
