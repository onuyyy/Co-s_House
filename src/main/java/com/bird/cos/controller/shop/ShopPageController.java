package com.bird.cos.controller.shop;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShopPageController {

    @GetMapping("/shop")
    public String shop() {
        return "redirect:/product";
    }
}
