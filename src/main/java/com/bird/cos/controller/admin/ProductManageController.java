package com.bird.cos.controller.admin;

import com.bird.cos.dto.admin.*;
import com.bird.cos.service.admin.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
@Controller
public class ProductManageController {

    private final AdminService adminService;

    // 1차 카테고리
    @GetMapping("/categories-1")
    public String getProductCategoryLevel1(Model model)
    {
        model.addAttribute("category_level1", adminService.getProductCategoryLevel1());

        return "admin/product/create-form";
    }

    // Ajax: 자식 카테고리 조회
    @GetMapping("/categories/{parentId}/children")
    @ResponseBody
    public List<ProductCategoryResponse> getChildCategories(@PathVariable Long parentId) {
        return adminService.getChildCategories(parentId);
    }

    @GetMapping("/new")
    public String createProduct( Model model)
    {
        try {
            List<BrandManageResponse> brandList = adminService.getBrandList();
            List<ProductCategoryResponse> categoryList = adminService.getProductCategoryLevel1();

            model.addAttribute("brandList", brandList);
            model.addAttribute("category_level1", categoryList);

            return "admin/product/create-form";
        } catch (Exception e) {
            log.error("createProduct 상품 등록 폼 로드 오류 {}",e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    // 상품 등록
    @PostMapping
    public String createProduct(@Valid ProductCreateRequest request, BindingResult bindingResult)
    {
        // @Valid 유효성 제약 조건의 검증 결과는 담는 객체
        if (bindingResult.hasErrors()) {
            log.info("createProduct binding error: {}", Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
            return "admin/product/create-form";
        }
        adminService.createProduct(request);

        return "redirect:/api/admin/products";
    }

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
        return "admin/product/product-list";
    }

    @GetMapping("/{productId}")
    public String productDetailPage(
            @PathVariable Long productId, Model model
    ) {
        model.addAttribute("product", adminService.getProductDetail(productId));
        return "admin/product/product-detail";
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
