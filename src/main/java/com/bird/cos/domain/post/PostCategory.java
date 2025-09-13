package com.bird.cos.domain.post;

import jakarta.persistence.*;

@Entity
@Table(name = "POST_CATEGORY")
public class PostCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_category_id")
    private Long postCategoryId;

    @Column(name = "post_category_name", length = 100, nullable = false)
    private String postCategoryName;

    @Column(name = "post_category_type", length = 50, nullable = false)
    private String postCategoryType;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

}