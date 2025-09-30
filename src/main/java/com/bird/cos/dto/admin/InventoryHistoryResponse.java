package com.bird.cos.dto.admin;

import com.bird.cos.domain.inventory.InventoryHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHistoryResponse {

    private Long historyId;
    private Long productId;
    private String productName;
    private Long inventoryId;
    private Long receiptId;
    private Long outboundId;
    private Integer changeQuantity;
    private Integer afterQuantity;
    private String changeType; // "입고", "출고"
    private LocalDateTime changeDate;
    private String receiptStatus; // 입고 상태 (PENDING, COMPLETED, CANCELLED)

    public static InventoryHistoryResponse from(InventoryHistory inventoryHistory) {
        return new InventoryHistoryResponse(
                inventoryHistory.getHistoryId(),
                inventoryHistory.getProductId() != null ? inventoryHistory.getProductId().getProductId() : null,
                inventoryHistory.getProductId() != null ? inventoryHistory.getProductId().getProductTitle() : null,
                inventoryHistory.getInventoryId() != null ? inventoryHistory.getInventoryId().getInventoryId() : null,
                inventoryHistory.getReceiptId() != null ? inventoryHistory.getReceiptId().getReceiptId() : null,
                inventoryHistory.getOutboundId() != null ? inventoryHistory.getOutboundId().getOutboundId() : null,
                inventoryHistory.getChangeQuantity(),
                inventoryHistory.getAfterQuantity(),
                inventoryHistory.getReceiptId() != null ? "입고" : (inventoryHistory.getOutboundId() != null ? "출고" : "조정"),
                inventoryHistory.getChangeDate(),
                inventoryHistory.getReceiptId() != null ? inventoryHistory.getReceiptId().getReceiptStatus() : null
        );
    }
}