package com.bird.cos.controller.support;

import com.bird.cos.dto.support.NoticeResponse;
import com.bird.cos.service.support.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int PAGE_BLOCK_SIZE = 5;
    private final NoticeService noticeService;

    @GetMapping
    public String listNotices(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        int pageIndex = Math.max(page, 0);
        int pageSize = size > 0 ? size : DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "noticeCreateDate"));
        Page<NoticeResponse> noticePage = noticeService.getAllNotices(pageable);

        int totalPages = noticePage.getTotalPages();
        int currentPage = noticePage.getNumber();

        List<Integer> pageNumbers = Collections.emptyList();
        if (totalPages > 0) {
            int startPage = Math.max(0, currentPage - (PAGE_BLOCK_SIZE / 2));
            int endPage = Math.min(totalPages - 1, startPage + PAGE_BLOCK_SIZE - 1);
            startPage = Math.max(0, endPage - PAGE_BLOCK_SIZE + 1);
            pageNumbers = IntStream.rangeClosed(startPage, endPage)
                    .boxed()
                    .collect(Collectors.toList());
        }

        model.addAttribute("noticePage", noticePage);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", noticePage.getSize());

        return "support/notice-list";
    }

    // 단건 공지 조회
    @GetMapping("/{id}")
    public String viewNotice(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticeService.getNotice(id));
        return "support/notice-detail";
    }
}
