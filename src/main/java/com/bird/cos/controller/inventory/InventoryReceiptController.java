package com.bird.cos.controller.inventory;

import com.bird.cos.dto.admin.InventoryReceiptRequest;
import com.bird.cos.dto.admin.InventoryReceiptResponse;
import com.bird.cos.dto.admin.InventoryHistoryResponse;
import com.bird.cos.service.inventory.InventoryReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/inventory")
public class InventoryReceiptController {

    private final InventoryReceiptService inventoryReceiptService;

    // 입고 처리
    @PostMapping("/receipt")
    public ResponseEntity<InventoryReceiptResponse> processReceipt(@RequestBody InventoryReceiptRequest request) {
        InventoryReceiptResponse response = inventoryReceiptService.processReceipt(request);
        return ResponseEntity.ok(response);
    }

    // 재고 이력 조회
    @GetMapping("/history/{productId}")
    public Page<InventoryHistoryResponse> getInventoryHistory(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "changeDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return inventoryReceiptService.getInventoryHistoryPage(productId, pageable);
    }

    // 입고 상태 변경
    @PutMapping("/receipt/{receiptId}/status")
    public ResponseEntity<InventoryReceiptResponse> updateReceiptStatus(
            @PathVariable Long receiptId,
            @RequestParam String status) {
        InventoryReceiptResponse response = inventoryReceiptService.updateReceiptStatus(receiptId, status);
        return ResponseEntity.ok(response);
    }

}