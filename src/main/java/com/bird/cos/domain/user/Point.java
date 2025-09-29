package com.bird.cos.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 기존 포인트 엔티티 (EventService 등에서 사용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "POINT")
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Long pointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "point_amount", nullable = false)
    private Integer pointAmount;

    @Column(name = "point_description", length = 500)
    private String pointDescription;

    @Column(name = "point_created_at", nullable = false)
    @Builder.Default
    private LocalDateTime pointCreatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (pointCreatedAt == null) {
            pointCreatedAt = LocalDateTime.now();
        }
    }
}