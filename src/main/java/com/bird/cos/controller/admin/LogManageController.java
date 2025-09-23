package com.bird.cos.controller.admin;

import com.bird.cos.dto.admin.LogResponse;
import com.bird.cos.service.admin.log.LogManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/admin/log")
public class LogManageController {

    private final LogManagerService logManagerService;

    @GetMapping
    public String logMainPage(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String username,
            @PageableDefault(size = 20, sort = "activityTime",direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<LogResponse> logList = logManagerService.getLogList(startDate, endDate, username, pageable);

        model.addAttribute("logList", logList);

        return "admin/log/log-list";
    }
}
