package com.bird.cos.dto.admin;

import com.bird.cos.domain.inventory.InventoryReceipt;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReceiptResponse {

    private Long receiptId;
    private Long productId;
    private String productTitle;
    private Integer receiptQuantity;
    private LocalDate receiptDate;
    private String receiptStatus;

    public static InventoryReceiptResponse from(InventoryReceipt inventoryReceipt) {
        return new InventoryReceiptResponse(
                inventoryReceipt.getReceiptId(),
                inventoryReceipt.getProductId() != null ? inventoryReceipt.getProductId().getProductId() : null,
                inventoryReceipt.getProductId() != null ? inventoryReceipt.getProductId().getProductTitle() : null,
                inventoryReceipt.getReceiptQuantity(),
                inventoryReceipt.getReceiptDate(),
                inventoryReceipt.getReceiptStatus()
        );
    }
}