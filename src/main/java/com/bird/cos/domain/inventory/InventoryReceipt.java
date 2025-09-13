package com.bird.cos.domain.inventory;

import com.bird.cos.domain.proudct.Product;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "INVENTORY_RECEIPT")
public class InventoryReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Long receiptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product productId;

    @Column(name = "receipt_quantity")
    private Integer receiptQuantity;

    @Column(name = "receipt_status", length = 20, nullable = false)
    private String receiptStatus;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

}