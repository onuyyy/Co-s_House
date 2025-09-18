package com.bird.cos.domain.product;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "QUESTION")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_type", referencedColumnName = "code_id")
    private CommonCode questionType;

    @Column(name = "question_title", length = 255, nullable = false)
    private String questionTitle;

    @Column(name = "question_content", columnDefinition = "TEXT", nullable = false)
    private String questionContent;

    @Column(name = "is_secret")
    private Boolean isSecret = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_status", referencedColumnName = "code_id")
    private CommonCode questionStatus;

    @Column(name = "question_created_at", insertable = false, updatable = false)
    private LocalDateTime questionCreatedAt;

}