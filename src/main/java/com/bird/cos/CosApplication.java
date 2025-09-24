package com.bird.cos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CosApplication {

    public static void main(String[] args) {

        SpringApplication.run(CosApplication.class, args);
    }

}