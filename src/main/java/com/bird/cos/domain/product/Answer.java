package com.bird.cos.domain.product;

import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ANSWER")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answerer_id")
    private User answerer;

    @Column(name = "answer_content", columnDefinition = "TEXT", nullable = false)
    private String answerContent;

    @Column(name = "answer_created_at", insertable = false, updatable = false)
    private LocalDateTime answerCreatedAt;

    @Column(name = "answer_updated_at", insertable = false, updatable = false)
    private LocalDateTime answerUpdatedAt;

}