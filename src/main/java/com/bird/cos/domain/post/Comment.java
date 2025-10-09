package com.bird.cos.domain.post;

import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private Comment parentCommentId;

    @OneToMany(mappedBy = "parentCommentId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Comment> childComments = new ArrayList<>();

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "comment_created_at", insertable = false, updatable = false)
    private LocalDateTime commentCreatedAt;

    @Column(name = "comment_updated_at", insertable = false, updatable = false)
    private LocalDateTime commentUpdatedAt;

    public void addChildComment(Comment childComment) {
        this.childComments.add(childComment);
        // parentCommentId 설정은 Builder로 생성 시에 처리하도록 변경 필요
        // 또는 별도의 비즈니스 메서드로 처리
    }

}