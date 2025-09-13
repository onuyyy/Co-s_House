package com.bird.cos.domain.user;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_GRADE")
public class UserGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Integer gradeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    @Column(name = "purchase_count")
    private Integer purchaseCount = 0;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "grade_period_start", nullable = false)
    private LocalDate gradePeriodStart;

    @Column(name = "grade_period_end", nullable = false)
    private LocalDate gradePeriodEnd;

    @Column(name = "grade_created_at", insertable = false, updatable = false)
    private LocalDateTime gradeCreatedAt;

}