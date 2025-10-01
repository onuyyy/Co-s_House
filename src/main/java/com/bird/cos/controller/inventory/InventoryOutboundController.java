package com.bird.cos.controller.inventory;

import com.bird.cos.service.inventory.InventoryOutboundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/inventory/outbound")
@RestController
public class InventoryOutboundController {

    private final InventoryOutboundService inventoryOutboundService;

    /**
     * 주문 출고 처리 (재고 차감)
     * @param orderId 주문 ID
     */
    @PostMapping("/{orderId}")
    public ResponseEntity<Void> processOutbound(@PathVariable Long orderId) {
        inventoryOutboundService.processOutboundForOrder(orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * 주문 취소/환불 시 재고 복구 (재고 증가)
     * @param orderId 주문 ID
     */
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<Void> processInbound(@PathVariable Long orderId) {
        inventoryOutboundService.processInboundForOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
