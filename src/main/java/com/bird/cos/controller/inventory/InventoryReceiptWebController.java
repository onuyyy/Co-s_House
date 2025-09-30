package com.bird.cos.controller.inventory;

import com.bird.cos.dto.admin.InventoryReceiptResponse;
import com.bird.cos.service.inventory.InventoryReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/admin/inventory")
public class InventoryReceiptWebController {

    private final InventoryReceiptService inventoryReceiptService;

    // 입고 등록 페이지
    @GetMapping("/receipt")
    public String receiptForm() {
        return "admin/inventory/inventory-receipt";
    }

    // 입고 목록 페이지
    @GetMapping("/receipt-list")
    public String receiptList(
            @PageableDefault(size = 20, sort = "receiptDate", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        Page<InventoryReceiptResponse> receiptPage = inventoryReceiptService.getAllReceiptsPage(pageable);

        model.addAttribute("receiptPage", receiptPage);
        model.addAttribute("totalCount", receiptPage.getTotalElements());

        return "admin/inventory/inventory-receipt-list";
    }
}