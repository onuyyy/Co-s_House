package com.bird.cos.dto.post;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
public class PostRequest {
    private Long userId;          // 작성자 ID
    private String title;         // 게시글 제목
    private String content;       // 게시글 내용
    private List<MultipartFile> images;

    private String housingType;   // 주거 형태 (아파트, 원룸, 빌라 등)
    private Integer areaSize;     // 평수
    private Integer roomCount;    // 방 개수
    private String familyType;    // 가족 형태 (싱글, 신혼, 아이 있음 등)
    private Boolean hasPet;       // 반려동물 유무
    private Integer familyCount;  // 가족 구성원 수
    private String projectType;   // 작업 분야 (리모델링, 홈스타일링, 부분시공 등)
    private Boolean isPublic; // 공개여부
}
