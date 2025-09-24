package com.bird.cos.controller.admin;

import com.bird.cos.dto.admin.BrandCreateRequest;
import com.bird.cos.dto.admin.BrandManageResponse;
import com.bird.cos.dto.admin.BrandManageSearchType;
import com.bird.cos.dto.admin.BrandUpdateRequest;
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

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/brands")
@Controller
public class BrandManageController {

    private final AdminService adminService;

    @GetMapping("/new")
    public String createBrand()
    {
        return "admin/brand/create-form";
    }

    @PostMapping
    public String createBrand(@Valid BrandCreateRequest request, BindingResult bindingResult)
    {
        if (bindingResult.hasErrors()) {
            log.info("createBrand binding error: {}", Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
            return "admin/brand/create-form";
        }

        adminService.createBrand(request);

        return "redirect:/api/admin/brands";
    }

    @GetMapping
    public String brandManagePage(
            @RequestParam(required = false, defaultValue = "NAME") BrandManageSearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 20, sort = "brandName", direction = Sort.Direction.ASC) Pageable pageable,
            Model model
    ) {
        Page<BrandManageResponse> brandList =
                adminService.getBrandList(searchType, searchValue, pageable);

        model.addAttribute("brandList", brandList);
        return "admin/brand/brand-list";
    }

    @GetMapping("/{brandId}")
    public String brandDetailPage(
            @PathVariable Long brandId, Model model
    ) {
        model.addAttribute("brand", adminService.getBrandDetail(brandId));
        return "admin/brand/brand-detail";
    }

    @PostMapping("/{brandId}/update")
    public String updateBrand(
            @PathVariable Long brandId,
            BrandUpdateRequest request
    ) {
        try {
            adminService.updateBrand(brandId, request);
            return "redirect:/api/admin/brands/" + brandId + "?success=true";
        } catch (RuntimeException e) {
            return "redirect:/api/admin/brands/" + brandId + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/{brandId}/delete")
    public String deleteBrand(
            @PathVariable Long brandId
    ) {
        try {
            adminService.deleteBrand(brandId);
            return "redirect:/api/admin/brands?deleted=true";
        } catch (RuntimeException e) {
            return "redirect:/api/admin/brands/" + brandId + "?error=" + e.getMessage();
        }
    }
}
