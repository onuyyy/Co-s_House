package com.bird.cos.controller.inventory;

import com.bird.cos.dto.admin.InventoryManageResponse;
import com.bird.cos.dto.admin.InventorySearchRequest;
import com.bird.cos.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/inventory")
@Controller
public class InventoryController {
    private final InventoryService inventoryService;

    // 재고 관리 메인 페이지 - 페이징된 재고 목록 조회 및 검색
    @GetMapping
    public String inventoryList(
            @ModelAttribute InventorySearchRequest searchRequest,
            @PageableDefault(size = 20, sort = "inventoryId", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        Page<InventoryManageResponse> inventoryPage;

        // 검색 조건이 있으면 검색, 없으면 전체 조회
        if (searchRequest.hasSearchCondition()) {
            inventoryPage = inventoryService.searchInventory(searchRequest, pageable);
            log.info("재고 검색 - ProductID: {}, Status: {}, 결과: {}개",
                    searchRequest.getProductId(), searchRequest.getInventoryStatus(), inventoryPage.getTotalElements());
        } else {
            inventoryPage = inventoryService.getAllInventoryPage(pageable);
        }

        model.addAttribute("inventoryPage", inventoryPage);
        model.addAttribute("inventoryList", inventoryPage.getContent());
        model.addAttribute("totalCount", inventoryPage.getTotalElements());
        model.addAttribute("searchRequest", searchRequest); // 검색 조건 유지용

        return "admin/inventory/inventory-list";
    }

    //재고 상세 조회
    @GetMapping("/{inventoryId}")
    public String inventoryDetail(@PathVariable Long inventoryId, Model model) {
        InventoryManageResponse inventory = inventoryService.getInventoryById(inventoryId);
        model.addAttribute("inventory", inventory);
        return "admin/inventory/inventory-detail";
    }
}
