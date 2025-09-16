package com.bird.cos.controller.admin;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Controller
public class AdminMainController {

    @RequestMapping("/main")
    public String adminMainPage() {
        return "forward:/api/admin/users";
    }
}
