package com.bird.cos.domain.post;

import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "`LIKE`")
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "like_status")
    private Boolean likeStatus = false;

    @Column(name = "like_created_at", insertable = false, updatable = false)
    private LocalDateTime likeCreatedAt;

}