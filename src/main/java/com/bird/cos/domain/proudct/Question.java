// *4. Lombok 추가, @Builder.Default, @PrePersist로 questionCreatedAt 자동 설정
package com.bird.cos.domain.proudct;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "QUESTION")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default    // 이유: Builder 패턴 사용 시 기본값이 무시되는 문제 해결
                        // isSecret이 false가
    private Boolean isSecret = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_status", referencedColumnName = "code_id")
    private CommonCode questionStatus;

    @Column(name = "question_created_at", updatable = false) //자동 날짜 입력이 안돼서 insert=false 제거
    private LocalDateTime questionCreatedAt;

    @PrePersist
    protected void onCreate() {
        this.questionCreatedAt = LocalDateTime.now();
    }

}