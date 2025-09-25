package com.bird.cos.service.home.dto;

import java.math.BigDecimal;

public record HomeProductDto(
        Long id,
        String title,
        BigDecimal price,
        BigDecimal salePrice,
        BigDecimal discountRate,
        BigDecimal rating,
        Integer reviewCount
) {}