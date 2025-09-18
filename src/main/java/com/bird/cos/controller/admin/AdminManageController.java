package com.bird.cos.controller.admin;

import com.bird.cos.dto.admin.UserManageResponse;
import com.bird.cos.dto.admin.UserManageSearchType;
import com.bird.cos.dto.admin.UserUpdateRequest;
import com.bird.cos.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 관리 컨트롤러 (User 엔티티 기반, ADMIN/SUPER_ADMIN 역할만 관리)
 */
@RequiredArgsConstructor
@RequestMapping("/api/admin/admins")
@Controller
public class AdminManageController {

    private final AdminService adminService;

    @GetMapping
    public String adminManagePage(
            @RequestParam(required = false, defaultValue = "NAME") UserManageSearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 20, sort = "userName", direction = Sort.Direction.ASC) Pageable pageable,
            Model model
    ) {
        // 관리자 역할(ADMIN, SUPER_ADMIN)만 조회
        Page<UserManageResponse> adminList = 
                adminService.getAdminList(searchType, searchValue, pageable);

        model.addAttribute("adminList", adminList);
        model.addAttribute("pageTitle", "관리자 관리");
        return "admin/admin-list";
    }

    @GetMapping("/{adminId}")
    public String adminDetailPage(
            @PathVariable Long adminId, Model model
    ) {
        UserManageResponse admin = adminService.getUserDetail(adminId);
        
        // 관리자가 아닌 경우 접근 거부
        if (!admin.isAdmin()) {
            return "redirect:/api/admin/admins?error=관리자가 아닌 사용자입니다.";
        }
        
        model.addAttribute("admin", admin);
        return "admin/admin-detail";
    }

    @PostMapping("/{adminId}/update")
    public String updateAdmin(
            @PathVariable Long adminId,
            UserUpdateRequest request
    ) {
        try {
            adminService.updateUser(adminId, request);
            return "redirect:/api/admin/admins/" + adminId + "?success=true";
        } catch (RuntimeException e) {
            return "redirect:/api/admin/admins/" + adminId + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/{adminId}/delete")
    public String deleteAdmin(
            @PathVariable Long adminId
    ) {
        try {
            // 관리자인지 확인 후 삭제
            UserManageResponse admin = adminService.getUserDetail(adminId);
            if (!admin.isAdmin()) {
                return "redirect:/api/admin/admins?error=관리자가 아닌 사용자는 삭제할 수 없습니다.";
            }
            
            adminService.deleteUser(adminId);
            return "redirect:/api/admin/admins?deleted=true";
        } catch (RuntimeException e) {
            return "redirect:/api/admin/admins/" + adminId + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/{adminId}/change-role")
    public String changeAdminRole(
            @PathVariable Long adminId,
            @RequestParam Long userRoleId
    ) {
        try {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setUserRoleId(userRoleId);
            
            adminService.updateUser(adminId, request);
            return "redirect:/api/admin/admins/" + adminId + "?success=role_changed";
        } catch (RuntimeException e) {
            return "redirect:/api/admin/admins/" + adminId + "?error=" + e.getMessage();
        }
    }
}
