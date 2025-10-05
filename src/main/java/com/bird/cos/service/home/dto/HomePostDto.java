package com.bird.cos.service.home.dto;

public record HomePostDto(
        Long id,
        String title,
        String imageUrl,
        Integer commentCount
) {}

