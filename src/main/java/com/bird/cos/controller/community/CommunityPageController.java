package com.bird.cos.controller.community;

import com.bird.cos.service.home.HomeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommunityPageController {
    private final HomeService homeService;
    public CommunityPageController(HomeService homeService) { this.homeService = homeService; }

    @GetMapping("/community")
    public String community(Model model) {
        model.addAttribute("topPosts", homeService.getTopPublic());

        return "community/index";
    }
}

