package com.bird.cos.controller.inventory;

import com.bird.cos.dto.admin.InventoryReceiptRequest;
import com.bird.cos.dto.admin.InventoryReceiptResponse;
import com.bird.cos.service.inventory.InventoryReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/inventory/receipt")
public class InventoryReceiptController {

    private final InventoryReceiptService inventoryReceiptService;

    // 입고 처리
    @PostMapping
    public ResponseEntity<InventoryReceiptResponse> processReceipt(@RequestBody InventoryReceiptRequest request) {
        InventoryReceiptResponse response = inventoryReceiptService.processReceipt(request);
        return ResponseEntity.ok(response);
    }

}