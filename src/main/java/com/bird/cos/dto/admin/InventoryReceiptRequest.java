package com.bird.cos.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReceiptRequest {

    private Long productId;
    private Integer receiptQuantity;
    private LocalDate receiptDate;
    private String receiptStatus;

    public static InventoryReceiptRequest of(Long productId, Integer receiptQuantity, LocalDate receiptDate, String receiptStatus) {
        return new InventoryReceiptRequest(productId, receiptQuantity, receiptDate, receiptStatus);
    }
}