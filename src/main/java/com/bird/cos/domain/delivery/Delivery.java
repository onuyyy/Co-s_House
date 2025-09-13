package com.bird.cos.domain.delivery;

import com.bird.cos.domain.order.Order;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DELIVERY")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long deliveryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_info_id")
    private DeliveryInfo deliveryInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "tracking_number", length = 50, nullable = false)
    private String trackingNumber;

    @Column(name = "delivery_status", length = 20, nullable = false)
    private String deliveryStatus;

    @Column(name = "recipient_name", length = 50, nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone", length = 20, nullable = false)
    private String recipientPhone;

    @Column(name = "delivery_address", length = 500, nullable = false)
    private String deliveryAddress;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}