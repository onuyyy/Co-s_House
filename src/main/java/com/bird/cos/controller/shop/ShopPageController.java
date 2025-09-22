package com.bird.cos.controller.shop;

import com.bird.cos.service.home.HomeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShopPageController {
    private final HomeService homeService;
    public ShopPageController(HomeService homeService) { this.homeService = homeService; }

    @GetMapping("/shop")
    public String shop(Model model) {
        model.addAttribute("todayDeals", homeService.todayDeals(12));
        model.addAttribute("popularProducts", homeService.popularProducts(12));
        return "shop/index";
    }
}

