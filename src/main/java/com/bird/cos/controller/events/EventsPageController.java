package com.bird.cos.controller.events;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EventsPageController {
    @GetMapping("/events")
    public String events() {
        return "events/index";
    }
}

