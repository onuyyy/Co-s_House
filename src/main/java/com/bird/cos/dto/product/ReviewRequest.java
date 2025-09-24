package com.bird.cos.dto.product;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ReviewRequest {

    private Long userId;
    private BigDecimal rating;
    private String content;
    private List<MultipartFile> images; // 이미지 업로드
    private Boolean isVerifiedPurchase;
    private Boolean isPhotoReview;
    private String title;
    private Long optionId;

}

