package com.bird.cos.controller.error;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 에러 페이지 컨트롤러
 * 커스텀 에러 페이지 표시를 담당
 */
@Controller
@RequestMapping("/error")
public class ErrorController {

    /**
     * 403 Forbidden 에러 페이지
     */
    @GetMapping("/403")
    public String forbidden(Model model) {
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorMessage", "접근 권한이 없습니다");
        return "error/403";
    }
}