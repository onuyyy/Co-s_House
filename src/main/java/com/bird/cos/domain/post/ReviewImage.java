package com.bird.cos.domain.post;

import com.bird.cos.domain.product.Review;
import jakarta.persistence.*;

@Entity
@Table(name = "REVIEW_IMAGE")
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

}