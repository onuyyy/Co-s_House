package com.bird.cos.service.home.dto;

public record HomePostDto(
        Long id,
        String title,
        Integer likeCount,
        Integer commentCount
) {}

