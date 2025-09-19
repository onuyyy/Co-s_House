package com.bird.cos.controller.register;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/account")
public class RegisterPageController {

    @GetMapping("/register")
    public String registerPage() {
        // templates/register/register.html 렌더링
        return "register/register";
    }
}
