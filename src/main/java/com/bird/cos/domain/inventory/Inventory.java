package com.bird.cos.domain.inventory;

import com.bird.cos.domain.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

@Entity
@Table(name = "INVENTORY")
@Getter
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product productId;

    @Column(name = "current_quantity")
    private Integer currentQuantity;

    @Column(name = "safety_quantity", nullable = false)
    private Integer safetyQuantity;

    @Column(name = "inventory_created_date", nullable = false, insertable = false, updatable = false)
    private LocalDateTime inventoryCreatedDate;

    @Column(name = "inventory_updated_date", nullable = false, insertable = false, updatable = false)
    private LocalDateTime inventoryUpdatedDate;

    // 재고 수량 업데이트 메서드
    public void updateCurrentQuantity(Integer newQuantity) {
        this.currentQuantity = newQuantity;
    }
}
