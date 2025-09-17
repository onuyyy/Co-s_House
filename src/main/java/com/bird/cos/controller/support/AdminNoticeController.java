package com.bird.cos.controller.support;

import com.bird.cos.dto.support.AdminNoticeRequest;
import com.bird.cos.dto.support.NoticeResponse;
import com.bird.cos.service.support.AdminNoticeService;
import com.bird.cos.service.support.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;
    private final NoticeService noticeService;

    // 공지 생성 폼 페이지
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("notice", new AdminNoticeRequest());
        return "support/notice-create-admin"; // templates/adminNoticeCreate.html
    }

    // 공지 생성 처리
    @PostMapping
    public String createNotice(@ModelAttribute AdminNoticeRequest request) {
        adminNoticeService.createNotice(request);
        return "redirect:/admin/notices/list";
    }

    // 공지 수정 폼 페이지
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        NoticeResponse notice = noticeService.getNotice(id);
        model.addAttribute("notice", notice);
        return "support/notice-edit-admin"; // templates/adminNoticeEdit.html
    }

    // 공지 수정 처리
    @PostMapping("/edit/{id}")
    public String updateNotice(@PathVariable Long id, @ModelAttribute AdminNoticeRequest request) {
        adminNoticeService.updateNotice(id, request);
        return "redirect:/admin/notices/list";
    }

    // 공지 삭제
    @PostMapping("/delete/{id}")
    public String deleteNotice(@PathVariable Long id) {
        adminNoticeService.deleteNotice(id);
        return "redirect:/admin/notices/list";
    }

    // 관리자용 공지 목록
    @GetMapping("/list")
    public String listNotices(
            Model model,
            @RequestParam(defaultValue = "0") int page) {
        model.addAttribute("notices", noticeService.getAllNotices());
        return "support/notice-list-admin";
    }
}
