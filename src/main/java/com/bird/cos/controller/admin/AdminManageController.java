package com.bird.cos.controller.admin;

import com.bird.cos.dto.admin.*;
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
@RequestMapping("/api/admin/admins")
@Controller
public class AdminManageController {

    private final AdminService adminService;

    @GetMapping
    public String adminManagePage(
            @RequestParam(required = false, defaultValue = "NAME") AdminManagerSearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 20, sort = "adminName", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {

        Page<AdminManageResponse> adminList =
                adminService.getAdminList(searchType, searchValue, pageable);

        model.addAttribute("adminList", adminList);
        return "admin/admin-list";
    }

    @GetMapping("/{adminId}")
    public String adminDetailPage(
            @PathVariable Long adminId, Model model
    ) {
        model.addAttribute("admin", adminService.getAdminDetail(adminId));

        return "admin/admin-detail";
    }

    @PostMapping("/{adminId}/update")
    public String adminUpdate(
            @PathVariable Long adminId,
            AdminUpdateRequest request
    ) {

        adminService.updateAdmin(adminId, request);
        return "redirect:/api/admin/admins/" + adminId;
    }

    @PostMapping("/{adminId}/delete")
    public String adminDelete(
            @PathVariable Long adminId
    ) {
        adminService.deleteAdmin(adminId);

        return "redirect:/api/admin/admins";
    }
}
