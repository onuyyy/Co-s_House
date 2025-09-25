package com.bird.cos.controller.event;

import com.bird.cos.dto.events.EventActionResult;
import com.bird.cos.service.event.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bird.cos.domain.user.User;
import com.bird.cos.repository.user.UserRepository;

@Controller
@RequiredArgsConstructor
public class EventsPageController {

    private final EventService eventService;
    private final UserRepository userRepository;

    //이벤트 목록
    @GetMapping("/events")
    public String events(Model model) {
        model.addAttribute("events", eventService.getActiveEventCards());
        return "events/event-list";
    }

    //이벤트 상세
    @GetMapping("/events/{slug}")
    public String eventDetail(@PathVariable String slug, Model model) {
        model.addAttribute("event", eventService.getEventDetail(slug));
        return "events/detail";
    }

    //헤택받기
    @PostMapping("/events/{slug}/action")
    public String eventAction(@PathVariable String slug,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userRepository.findByUserEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        EventActionResult result = eventService.performEventAction(slug, user.getUserId());

        redirectAttributes.addFlashAttribute("actionMessage", result.getMessage());
        redirectAttributes.addFlashAttribute("actionSuccess", result.isSuccess());
        redirectAttributes.addFlashAttribute("actionCompleted", result.isCompleted());
        if (result.getPointAmount() != null) {
            redirectAttributes.addFlashAttribute("actionPointAmount", result.getPointAmount());
        }

        return "redirect:/events/" + slug;
    }
}
