package com.bird.cos.dto.admin;

import com.bird.cos.domain.inventory.InventoryOutbound;
import com.bird.cos.domain.order.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOutboundResponse {

    private Long outboundId;
    private Long productId;
    private String productTitle;
    private Order orderId;
    private Integer outboundQuantity;
    private LocalDate outboundDate;
    private String outboundStatus;
    private LocalDateTime outboundCreatedDate;

    public static InventoryOutboundResponse from(InventoryOutbound inventoryOutbound) {
        return new InventoryOutboundResponse(
                inventoryOutbound.getOutboundId(),
                inventoryOutbound.getProductId() != null ? inventoryOutbound.getProductId().getProductId() : null,
                inventoryOutbound.getProductId() != null ? inventoryOutbound.getProductId().getProductTitle() : null,
                inventoryOutbound.getOrderId(),
                inventoryOutbound.getOutboundQuantity(),
                inventoryOutbound.getOutboundDate(),
                inventoryOutbound.getOutboundStatus(),
                inventoryOutbound.getOutboundCreatedDate()
        );
    }
}