package com.bird.cos.dto.post;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostDetailResponse {
    private Long postId;
    private String title;
    private String content;
    private String username;
    private LocalDateTime publishDate;
    
    // 게시글 정보
    private String housingType;
    private Integer areaSize;
    private Integer roomCount;
    private String familyType;
    private Boolean hasPet;
    private Integer familyCount;
    private String projectType;
    
    // 통계
    private Integer viewCount;
    private Integer likeCount;
    private Integer scrapCount;
    
    // 이미지들
    private List<PostImageDto> images;
    
    // 연결된 상품들
    private List<PostProductDto> products;
    
    @Getter
    @Builder
    public static class PostImageDto {
        private Long imageId;
        private String imageUrl;
        private Integer displayOrder;
    }
    
    @Getter
    @Builder
    public static class PostProductDto {
        private Long productId;
        private String productTitle;
        private String mainImageUrl;
    }
}
