package com.bird.cos.repository.product;

import com.bird.cos.domain.product.Review;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

public class ReviewSpecificationRepository {

    // Product ID로 검색하는 조건
    public static Specification<Review> hasProductId(Long productId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("product").get("productId"), productId);
    }

    // Option ID로 검색하는 조건
    public static Specification<Review> hasOptionId(Long optionId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("productOption").get("optionId"), optionId);
    }

    // 별점 범위로 검색하는 조건
    public static Specification<Review> inRatingRange(String ratingRange) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("rating"), new BigDecimal(ratingRange));
    }

    // 포토리뷰만 검색하는 조건
    public static Specification<Review> isPhotoReview() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("isPhotoReview"));
    }

    // 구매자 리뷰만 검색하는 조건
    public static Specification<Review> isVerifiedPurchase() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("isVerifiedPurchase"));
    }
}