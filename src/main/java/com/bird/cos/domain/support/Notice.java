package com.bird.cos.domain.support;

import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Entity
@Table(name = "NOTICE")
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private  String content;

    @Column(name = "notice_created_date", nullable = false, updatable = false)
    private LocalDateTime noticeCreateDate;

    @Column(name = "notice_updated_date")
    private  LocalDateTime noticeUpdateDate;

    // 공지사항 업데이트
    public void update(String title, String content) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
        this.title = title.trim();
        this.content = content.trim();
        this.noticeUpdateDate = LocalDateTime.now();
    }
}
