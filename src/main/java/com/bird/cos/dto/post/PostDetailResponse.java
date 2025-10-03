package com.bird.cos.dto.post;

import com.bird.cos.domain.post.PostImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class PostDetailResponse {

    private Long postId;
    private String username;
    private List<PostImage> postImages = new ArrayList<>();
    private String title;
    private String content;
    private String housingType;   // 주거 형태 (아파트, 원룸 등)
    private Integer areaSize;     // 평수
    private Integer roomCount;    // 방 개수
    private String familyType;    // 가족 형태 (싱글, 신혼, 아이 있음 등)
    private Boolean hasPet;       // 반려동물 유무
    private Integer familyCount;  // 가족 구성원 수
    private String projectType;   // 작업 분야 (리모델링, 홈스타일링 등)
    private Boolean isPublic = true;
    private LocalDateTime postCreatedAt;
    private LocalDateTime postUpdatedAt;
}
