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
    private Long inventoryId;
    private Long receiptId;
    private Long outboundId;
    private Integer changeQuantity;
    private Integer afterQuantity;
    private LocalDateTime changeDate;

    public static InventoryHistoryResponse from(InventoryHistory inventoryHistory) {
        return new InventoryHistoryResponse(
                inventoryHistory.getHistoryId(),
                inventoryHistory.getProductId() != null ? inventoryHistory.getProductId().getProductId() : null,
                inventoryHistory.getInventoryId() != null ? inventoryHistory.getInventoryId().getInventoryId() : null,
                inventoryHistory.getReceiptId() != null ? inventoryHistory.getReceiptId().getReceiptId() : null,
                inventoryHistory.getOutboundId() != null ? inventoryHistory.getOutboundId().getOutboundId() : null,
                inventoryHistory.getChangeQuantity(),
                inventoryHistory.getAfterQuantity(),
                inventoryHistory.getChangeDate()
        );
    }
}