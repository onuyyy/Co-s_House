package com.bird.cos.controller.support;

import com.bird.cos.service.support.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 전체 공지 조회
    @GetMapping
    public String listNotices(Model model,
                              @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page,10);
        model.addAttribute("notices", noticeService.getAllNotices(pageable));
        return "support/notice-list";
    }

    // 단건 공지 조회
    @GetMapping("/{id}")
    public String viewNotice(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticeService.getNotice(id));
        return "support/notice-detail";
    }
}
