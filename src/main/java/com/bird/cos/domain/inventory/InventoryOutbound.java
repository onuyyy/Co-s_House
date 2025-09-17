package com.bird.cos.domain.inventory;

import com.bird.cos.domain.order.Order;
import com.bird.cos.domain.product.Product;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "INVENTORY_OUTBOUND")
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

    @Column(name = "outbound_created_date", nullable = false, insertable = false, updatable = false)
    private LocalDateTime outboundCreatedDate;

}
