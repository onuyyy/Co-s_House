package com.bird.cos.domain.admin;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CUSTOMER_SERVICE")
public class CustomerService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cs_id")
    private Long csId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "contact_type", length = 50, nullable = false)
    private String contactType;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "question_content", columnDefinition = "TEXT", nullable = false)
    private String questionContent;

    @Column(name = "customer_status", length = 50)
    private String customerStatus = "pending";

    @Column(name = "customer_created_at", insertable = false, updatable = false)
    private LocalDateTime customerCreatedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

}