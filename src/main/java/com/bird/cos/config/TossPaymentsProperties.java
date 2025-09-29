package com.bird.cos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TossPaymentsProperties.Property.class)
public class TossPaymentsProperties {

    @ConfigurationProperties(prefix = "toss.payments")
    public record Property(
            String secretKey,
            String clientKey,
            String successUrl,
            String failUrl
    ) {
    }
}

