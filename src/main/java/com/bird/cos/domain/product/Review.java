package com.bird.cos.domain.product;

import com.bird.cos.domain.order.OrderItem;
import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "REVIEW")
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Column(name = "review_title")
    private String title;

    @Column(name = "rating", nullable = false)
    private BigDecimal rating;

    @Column(name = "review_content", columnDefinition = "TEXT", nullable = false)
    private String reviewContent;

    @Builder.Default
    @Column(name = "is_verified_purchase", nullable = false)
    private Boolean isVerifiedPurchase = false;

    @Builder.Default
    @Column(name = "is_photo_review", nullable = false)
    private Boolean isPhotoReview = false;

    @CreatedDate
    @Column(name = "review_created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "review_updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private ProductOption productOption;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> reviewImages = new ArrayList<>();

    public void addReviewImage(ReviewImage reviewImage) {
        reviewImages.add(reviewImage);
        reviewImage.setReview(this); // 양방향 관계 설정
    }

    public void updateReviewContent(String title, String content, BigDecimal rating, ProductOption productOption) {
        this.title = title;
        this.reviewContent = content;
        this.rating = rating;
        this.productOption = productOption;
        // 옵션 ID는 ProductOption 객체 자체를 받아와서 설정해야 할 수 있습니다.
        // this.productOption = productOptionRepository.findById(optionId).orElseThrow(...)
        // 여기서는 간단히 필드 업데이트만 예시로 둡니다. 실제로는 ProductOption 객체를 받아와야 함.
    }

    // isPhotoReview 업데이트 (ReviewService에서 직접 해도 됨)
    public void setIsPhotoReview(boolean isPhotoReview) {
        this.isPhotoReview = isPhotoReview;
    }
}
