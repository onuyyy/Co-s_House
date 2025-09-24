package com.bird.cos.dto.product;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateRequest {
    private String title;
    private Integer rating;
    private String content;
    private Long optionId;

    //삭제할 기존 이미지들의 ID 목록
    private List<Long> deletedImageIds;

    //새로 업로드할 이미지 파일 목록
    private List<MultipartFile> newImageFiles;

    private List<Integer> deletedImageIndexes;
    private List<MultipartFile> newImages;
}