package com.bird.cos.domain.brand;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "EVENT")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_name", length = 255, nullable = false)
    private String eventName;

    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "event_description", columnDefinition = "TEXT", nullable = false)
    private String eventDescription;

    @Column(name = "event_start_date", nullable = false)
    private LocalDateTime eventStartDate;

    @Column(name = "event_end_date", nullable = false)
    private LocalDateTime eventEndDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "event_created_at", insertable = false, updatable = false)
    private LocalDateTime eventCreatedAt;

}