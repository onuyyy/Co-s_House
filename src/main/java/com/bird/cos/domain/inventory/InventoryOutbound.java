package com.bird.cos.domain.inventory;

import com.bird.cos.domain.order.Order;
import com.bird.cos.domain.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "INVENTORY_OUTBOUND")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryOutbound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbound_id")
    private Long outboundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product productId;

    @Column(name = "outbound_quantity", nullable = false)
    private Integer outboundQuantity;

    @Column(name = "outbound_status", length = 20, nullable = false)
    private String outboundStatus;

    @Column(name = "outbound_date", nullable = false)
    private LocalDate outboundDate;

    @CreationTimestamp
    @Column(name = "outbound_created_date", nullable = false, updatable = false)
    private LocalDateTime outboundCreatedDate;

    @Builder
    public InventoryOutbound(Order orderId, Product productId, Integer outboundQuantity,
                            String outboundStatus, LocalDate outboundDate) {
        this.orderId = orderId;
        this.productId = productId;
        this.outboundQuantity = outboundQuantity;
        this.outboundStatus = outboundStatus;
        this.outboundDate = outboundDate;
    }
}
