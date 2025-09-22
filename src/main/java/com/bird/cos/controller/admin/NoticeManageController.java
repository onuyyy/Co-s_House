package com.bird.cos.controller.admin;

import com.bird.cos.dto.support.NoticeRequest;
import com.bird.cos.dto.support.NoticeResponse;
import com.bird.cos.service.support.NoticeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@RequestMapping("/api/admin/notices")
@Controller
public class NoticeManageController {

    private final NoticeService noticeService;

    // 공지 목록 페이지
    @GetMapping
    public String noticeManagePage(
            @PageableDefault(size = 20, sort = "noticeCreateDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            Model model
    ) {
        Page<NoticeResponse> noticeList;
        String trimmedSearch = search != null ? search.trim() : null;
        String normalizedType = (type == null || type.isBlank()) ? "all" : type;

        if (trimmedSearch != null && !trimmedSearch.isEmpty()) {
            switch (normalizedType) {
                case "title" -> noticeList = noticeService.searchNoticesByTitle(trimmedSearch, pageable);
                case "content" -> noticeList = noticeService.searchNoticesByContent(trimmedSearch, pageable);
                default -> noticeList = noticeService.searchNotices(trimmedSearch, pageable);
            }
        } else {
            noticeList = noticeService.getAllNotices(pageable);
            trimmedSearch = null; // 검색어 초기화
        }

        model.addAttribute("noticeList", noticeList);
        model.addAttribute("currentPage", pageable.getPageNumber());
        model.addAttribute("totalPages", noticeList.getTotalPages());
        model.addAttribute("search", trimmedSearch);
        model.addAttribute("type", normalizedType);
        model.addAttribute("pageTitle", "공지 관리");
        model.addAttribute("totalElements", noticeList.getTotalElements());

        return "admin/admin-notice-list";
    }

    // 상세 페이지
    @GetMapping("/{noticeId}/detail")
    public String noticeDetailPage(@PathVariable Long noticeId, Model model) {
        try {
            NoticeResponse notice = noticeService.getNotice(noticeId);
            model.addAttribute("notice", notice);
            return "admin/admin-notice-detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "공지사항을 찾을 수 없습니다.");
            return "admin/admin-notice-list";
        }
    }

    // 공지 생성 페이지
    @GetMapping("/create")
    public String createNoticePage(Model model) {
        model.addAttribute("notice", new NoticeRequest());
        return "admin/admin-notice-create";
    }

    // 공지 생성 처리
    @PostMapping("/create")
    public String createNotice(@ModelAttribute @Valid NoticeRequest request,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        try {
            //시큐리티로 변경가능 지금은 세션처리
            Long writerId = (Long) session.getAttribute("userId");
            if (writerId == null) {
                redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
                return "redirect:/controller/register/login";
            }

            NoticeResponse createdNotice = noticeService.createNotice(request, writerId);
            redirectAttributes.addFlashAttribute("success", "공지사항이 성공적으로 등록되었습니다.");
            return "redirect:/api/admin/notices/" + createdNotice.getNoticeId() + "/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "공지사항 등록에 실패했습니다: " + e.getMessage());
            return "redirect:/api/admin/notices/create";
        }
    }

    // 수정 페이지
    @GetMapping("/{noticeId}/edit")
    public String editNoticePage(@PathVariable Long noticeId, Model model) {
        try {
            NoticeResponse notice = noticeService.getNotice(noticeId);

            // Response를 Request로 변환
            NoticeRequest request = NoticeRequest.builder()
                    .title(notice.getTitle())
                    .content(notice.getContent())
                    .build();

            model.addAttribute("notice", request);
            model.addAttribute("noticeId", noticeId);
            return "admin/admin-notice-detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "공지사항을 찾을 수 없습니다.");
            return "admin/admin-notice-detail";
        }
    }

    // 수정 처리
    @PostMapping("/{noticeId}/update")
    public String updateNotice(@PathVariable Long noticeId,
                               @ModelAttribute @Valid NoticeRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            noticeService.updateNotice(noticeId, request);
            redirectAttributes.addFlashAttribute("success", "공지사항이 성공적으로 수정되었습니다.");
            return "redirect:/api/admin/notices/" + noticeId + "/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "공지사항 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/api/admin/notices/" + noticeId + "/edit";
        }
    }

    // 삭제 처리
    @PostMapping("/{noticeId}/delete")
    public String deleteNotice(@PathVariable Long noticeId,
                               RedirectAttributes redirectAttributes) {
        try {
            noticeService.deleteNotice(noticeId);
            redirectAttributes.addFlashAttribute("success", "공지사항이 성공적으로 삭제되었습니다.");
            return "redirect:/api/admin/notices";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "공지사항 삭제에 실패했습니다: " + e.getMessage());
            return "redirect:/api/admin/notices";
        }
    }
}
