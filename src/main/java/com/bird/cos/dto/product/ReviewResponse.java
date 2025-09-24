package com.bird.cos.dto.product;

import com.bird.cos.domain.product.ProductOption;
import com.bird.cos.domain.product.Review;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public class ReviewResponse {
    private Long reviewId;
    private Long productId;
    private String userNickname;
    private String title;
    private BigDecimal rating;
    private String content;
    private String nickname;
    private List<String> imageUrls;
    private Boolean isVerifiedPurchase;
    private Boolean isPhotoReview;
    private LocalDateTime createdAt;

    // 옵션 정보 추가
    private Long optionId;
    private String optionName;  // 옵션명 혹은 옵션식별용

    public ReviewResponse(Long reviewId, String userNickname, String title, BigDecimal rating, String content,
                          Boolean isVerifiedPurchase, Boolean isPhotoReview, LocalDateTime createdAt,
                          Long optionId, String optionName) {
        this.reviewId = reviewId;        // reviewId 추가
        this.userNickname = userNickname;
        this.title = title;
        this.rating = rating;
        this.content = content;
        this.isVerifiedPurchase = isVerifiedPurchase;
        this.isPhotoReview = isPhotoReview;
        this.createdAt = createdAt;
        this.optionId = optionId;
        this.optionName = optionName;
    }

    public static ReviewResponse fromEntity(Review review) {
        ProductOption productOption = review.getProductOption();

        BigDecimal rating = review.getRating() != null ? review.getRating() : BigDecimal.ZERO;
        String userNickname = review.getUser() != null ? review.getUser().getNickname() : "탈퇴한 사용자";

        ReviewResponse response = new ReviewResponse(
                review.getReviewId(),
                userNickname,
                review.getTitle(),
                rating,
                review.getReviewContent(),
                review.getIsVerifiedPurchase(),
                review.getIsPhotoReview(),
                review.getCreatedAt(),
                productOption != null ? productOption.getOptionId() : null,
                productOption != null ? productOption.getOptionName() : null
        );
        if (review.getProduct() != null) {
            response.setProductId(review.getProduct().getProductId());
        }
        // 이미지 URL 목록 매핑 (저장된 파일명 -> 접근 가능한 URL)
        if (review.getReviewImages() != null) {
            response.imageUrls = review.getReviewImages().stream()
                    .map(image -> "/images/" + image.getStoredFileName()) // URL 형식으로 변환
                    .collect(Collectors.toList());
        } else {
            response.imageUrls = Collections.emptyList();
        }

        // isPhotoReview도 실제 이미지 존재 유무로 판단
        response.isPhotoReview = (response.imageUrls != null && !response.imageUrls.isEmpty());

        return response;
    }

    public String getStarRating() {
        if (rating == null) {
            return "☆☆☆☆☆";
        }
        int fullStars = rating.intValue();
        StringBuilder stars = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            if (i < fullStars) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }

    public Integer getIntegerRating() {
        if (rating == null) {
            return 0;
        }
        return rating.intValue(); // BigDecimal을 정수로 변환
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
}