package com.bird.cos.repository.product;

import com.bird.cos.domain.product.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 기본 조회
    List<Review> findAllByOrderByCreatedAtDesc();;
    List<Review> findByProduct_ProductIdOrderByCreatedAtDesc(Long productId);

    // 필터별 조회 - 전체
    List<Review> findByIsPhotoReviewTrueOrderByCreatedAtDesc();
    List<Review> findByIsVerifiedPurchaseTrueOrderByCreatedAtDesc();

    // 상품 조회
    List<Review> findByProduct_ProductIdAndProductOption_OptionId(Long productId, Long optionId);

    // 필터별 조회 - 상품별
    List<Review> findByProduct_ProductIdAndIsPhotoReviewTrueOrderByCreatedAtDesc(Long productId);
    List<Review> findByProduct_ProductIdAndIsVerifiedPurchaseTrueOrderByCreatedAtDesc(Long productId);


    // 평점별 정렬
    List<Review> findAllByOrderByRatingDesc();
    List<Review> findAllByOrderByRatingAsc();
    List<Review> findByProduct_ProductIdOrderByRatingDesc(Long productId);
    List<Review> findByProduct_ProductIdOrderByRatingAsc(Long productId);

    // 통계를 위한 쿼리
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productId = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.productId = :productId")
    Long countByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.productId = :productId AND r.isPhotoReview = true")
    Long countPhotoReviewsByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.productId = :productId AND r.isVerifiedPurchase = true")
    Long countVerifiedReviewsByProductId(@Param("productId") Long productId);

    // 포토리뷰 조회 메서드
    @Query("SELECT r FROM Review r WHERE r.product.productId = :productId AND SIZE(r.reviewImages) > 0")
    Page<Review> findPhotoReviewsByProductId(@Param("productId") Long productId, Pageable pageable);

    // 옵션별 포토리뷰 조회 메서드
    @Query("SELECT r FROM Review r WHERE r.product.productId = :productId AND r.productOption.optionId = :optionId AND SIZE(r.reviewImages) > 0")
    Page<Review> findPhotoReviewsByProductIdAndOptionId(@Param("productId") Long productId, @Param("optionId") Long optionId, Pageable pageable);

    List<Review> findByProduct_ProductId(Long productId);

    Page<Review> findByProduct_ProductId(Long productId, Pageable pageable);

    Page<Review> findByProduct_ProductIdAndProductOption_OptionId(Long productId, Long optionId, Pageable pageable);


}
