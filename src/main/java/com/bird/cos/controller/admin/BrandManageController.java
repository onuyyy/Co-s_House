package com.bird.cos.controller.admin;

import com.bird.cos.dto.admin.BrandManageResponse;
import com.bird.cos.dto.admin.BrandManageSearchType;
import com.bird.cos.dto.admin.BrandUpdateRequest;
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
@RequestMapping("/api/admin/brands")
@Controller
public class BrandManageController {

    private final AdminService adminService;

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
        return "admin/brand-list";
    }

    @GetMapping("/{brandId}")
    public String brandDetailPage(
            @PathVariable Long brandId, Model model
    ) {
        model.addAttribute("brand", adminService.getBrandDetail(brandId));
        return "admin/brand-detail";
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
