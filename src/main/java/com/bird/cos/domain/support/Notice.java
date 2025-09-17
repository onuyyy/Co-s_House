package com.bird.cos.domain.support;

import com.bird.cos.domain.admin.Admin;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
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
    @JoinColumn(name = "admin_id")
    private Admin adminId;

    @Column(name = "title", length = 2555, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private  String content;

    @Column(name = "notice_created_date", nullable = false, updatable = false)
    private LocalDateTime noticeCreateDate;

    @Column(name = "notice_updated_date")
    private  LocalDateTime noticeUpdateDate;

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.noticeUpdateDate = LocalDateTime.now();
    }
}
