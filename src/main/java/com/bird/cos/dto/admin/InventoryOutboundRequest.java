package com.bird.cos.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOutboundRequest {

    private Long productId;
    private Long orderId;
    private Integer outboundQuantity;
    private LocalDate outboundDate;
    private String outboundStatus;

    public static InventoryOutboundRequest of(Long productId, Long orderId, Integer outboundQuantity, LocalDate outboundDate, String outboundStatus) {
        return new InventoryOutboundRequest(productId, orderId, outboundQuantity, outboundDate, outboundStatus);
    }
}