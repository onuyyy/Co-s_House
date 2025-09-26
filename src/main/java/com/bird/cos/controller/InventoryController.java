package com.bird.cos.controller;

import com.bird.cos.dto.admin.InventoryManageResponse;
import com.bird.cos.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/inventory")
@Controller
public class InventoryController {
    private final InventoryService inventoryService;

    //재고 관리 메인 페이지 - 전체 재고 목록 조회

    @GetMapping("")
    public String inventoryList(Model model) {
        List<InventoryManageResponse> inventoryList = inventoryService.getAllInventory();
        long totalCount = inventoryService.getTotalCount();

        model.addAttribute("inventoryList", inventoryList);
        model.addAttribute("totalCount", totalCount);

        return "admin/inventory/inventory-list";
    }
}
