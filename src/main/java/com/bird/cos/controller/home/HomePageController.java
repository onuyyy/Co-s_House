package com.bird.cos.controller.home;

import com.bird.cos.service.home.HomeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePageController {

    private final HomeService homeService;

    public HomePageController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("todayDeals", homeService.todayDeals(8));
        model.addAttribute("popularProducts", homeService.popularProducts(8));
        model.addAttribute("topPosts", homeService.getTopPublic());
        model.addAttribute("highlightEvents", homeService.highlightEvents());
        return "home/index"; // templates/home/index.html
    }
}
