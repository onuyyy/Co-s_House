package com.bird.cos.domain.user;

import com.bird.cos.domain.common.CommonCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "point_amount", nullable = false)
    private Integer pointAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pointType", referencedColumnName = "code_id")
    private CommonCode pointType;

    @Column(name = "point_description", length = 255, nullable = false)
    private String pointDescription;

    @Column(name = "point_created_at", insertable = false, updatable = false)
    private LocalDateTime pointCreatedAt;

}