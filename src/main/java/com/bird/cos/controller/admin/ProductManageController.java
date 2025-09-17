package com.bird.cos.controller.admin;

import com.bird.cos.dto.admin.ProductManageResponse;
import com.bird.cos.dto.admin.ProductManageSearchType;
import com.bird.cos.dto.admin.ProductUpdateRequest;
import com.bird.cos.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
@Controller
public class ProductManageController {

    private final AdminService adminService;

    @GetMapping
    public String productManagePage(
            @RequestParam(required = false, defaultValue = "TITLE") ProductManageSearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 20, sort = "productCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<ProductManageResponse> productList =
                adminService.getProductList(searchType, searchValue, pageable);

        model.addAttribute("productList", productList);
        return "admin/product-list";
    }

    @GetMapping("/{productId}")
    public String productDetailPage(
            @PathVariable Long productId, Model model
    ) {
        model.addAttribute("product", adminService.getProductDetail(productId));
        return "admin/product-detail";
    }

    @PostMapping("/{productId}/update")
    public String updateProduct(
            @PathVariable Long productId,
            ProductUpdateRequest request
    ) {
        try {
            adminService.updateProduct(productId, request);
            return "redirect:/api/admin/products/" + productId + "?success=true";
        } catch (RuntimeException e) {
            return "redirect:/api/admin/products/" + productId + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/{productId}/delete")
    public String deleteProduct(
            @PathVariable Long productId
    ) {
        try {
            adminService.deleteProduct(productId);
            return "redirect:/api/admin/products?deleted=true";
        } catch (RuntimeException e) {
            return "redirect:/api/admin/products/" + productId + "?error=" + e.getMessage();
        }
    }
}
