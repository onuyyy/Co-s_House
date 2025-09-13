package com.bird.cos.domain.post;

import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "COMMENT")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    @Setter
    private Comment parentCommentId;

    @OneToMany(mappedBy = "parentCommentId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Comment> childComments = new ArrayList<>();

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "report_count")
    private Integer reportCount = 0;

    @Column(name = "comment_created_at", insertable = false, updatable = false)
    private LocalDateTime commentCreatedAt;

    @Column(name = "comment_updated_at", insertable = false, updatable = false)
    private LocalDateTime commentUpdatedAt;

    public void addChildComment(Comment childComment) {
        this.childComments.add(childComment);
        childComment.setParentCommentId(this);
    }

}