package com.bird.cos.domain.post;

import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "POST")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_category_id", nullable = true)
    private PostCategory postCategory;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "report_count")
    private Integer reportCount = 0;

    @Column(name = "post_created_at", insertable = false, updatable = false)
    private LocalDateTime postCreatedAt;

    @Column(name = "post_updated_at", insertable = false, updatable = false)
    private LocalDateTime postUpdatedAt;

    @Column(name = "post_updated_by")
    private Long postUpdatedBy;

}