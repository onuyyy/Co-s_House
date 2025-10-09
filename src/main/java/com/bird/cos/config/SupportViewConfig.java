package com.bird.cos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SupportViewConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/support/faq").setViewName("support/faq");
        registry.addViewController("/support/direct").setViewName("support/direct");
        registry.addViewController("/support/email").setViewName("support/email");
    }
}
