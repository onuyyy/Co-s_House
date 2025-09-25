package com.bird.cos.dto.admin;

import com.bird.cos.domain.inventory.Inventory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryManageResponse {

    private Long inventoryId;
    private Long productId;
    private String productTitle;
    private String mainImageUrl;
    private Integer currentQuantity;
    private Integer safetyQuantity;
    private LocalDateTime inventoryCreatedDate;
    private LocalDateTime inventoryUpdatedDate;

    public static InventoryManageResponse from(Inventory inventory) {
        return new InventoryManageResponse(
                inventory.getInventoryId(),
                inventory.getProductId() != null ? inventory.getProductId().getProductId() : null,
                inventory.getProductId() != null ? inventory.getProductId().getProductTitle() : null,
                inventory.getProductId() != null ? inventory.getProductId().getMainImageUrl() : null,
                inventory.getCurrentQuantity(),
                inventory.getSafetyQuantity(),
                inventory.getInventoryCreatedDate(),
                inventory.getInventoryUpdatedDate()
        );
    }
}
