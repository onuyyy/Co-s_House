package com.bird.cos.dto.post;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponse {
    private Long postId;
    private String thumbnail;
    private String title;
    private String username;
    private LocalDateTime publishDate;
    private Long scrapCount;
    private Integer viewCount;
    private boolean isRecent;
    private boolean isScraped;
}
