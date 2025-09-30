package com.bird.cos.domain.inventory;

import com.bird.cos.domain.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "INVENTORY_HISTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    private Inventory inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    private InventoryReceipt receiptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_id")
    private InventoryOutbound outboundId;

    @Column(name = "change_quantity", nullable = false)
    private Integer changeQuantity;

    @Column(name = "after_quantity", nullable = false)
    private Integer afterQuantity;

    @CreationTimestamp
    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    @Builder
    public InventoryHistory(Product productId, Inventory inventoryId, InventoryReceipt receiptId,
                           InventoryOutbound outboundId, Integer changeQuantity, Integer afterQuantity) {
        this.productId = productId;
        this.inventoryId = inventoryId;
        this.receiptId = receiptId;
        this.outboundId = outboundId;
        this.changeQuantity = changeQuantity;
        this.afterQuantity = afterQuantity;
    }
}
