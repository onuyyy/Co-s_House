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

@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@Controller
public class UserManageController {

    private final AdminService adminService;

    @GetMapping
    public String userManagePage(
            @RequestParam(required = false) String roleFilter,
            @RequestParam(required = false, defaultValue = "NAME") UserManageSearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 20, sort = "userName", direction = Sort.Direction.ASC) Pageable pageable,
            Model model
    ) {
        Page<UserManageResponse> userList;
        String pageTitle;

        if (roleFilter != null && !roleFilter.isEmpty()) {
            switch (roleFilter) {
                case "USER":
                    userList = adminService.getUsersByRole("USER", searchType, searchValue, pageable);
                    pageTitle = "일반 사용자 관리";
                    break;
                case "ADMIN":
                    userList = adminService.getAdminList(searchType, searchValue, pageable);
                    pageTitle = "관리자 관리";
                    break;
                default:
                    userList = adminService.getAllUsers(searchType, searchValue, pageable);
                    pageTitle = "전체 사용자 관리";
                    roleFilter = "ALL";
                    break;
            }
        } else {
            userList = adminService.getAllUsers(searchType, searchValue, pageable);
            pageTitle = "전체 사용자 관리";
            roleFilter = "ALL";
        }

        model.addAttribute("userList", userList);
        model.addAttribute("roleFilter", roleFilter);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("roles", adminService.getAllRoles());

        return "admin/user-list";
    }

    @GetMapping("/{userId}")
    public String userDetailPage(
            @PathVariable Long userId, Model model
    ) {
        UserManageResponse user = adminService.getUserDetail(userId);
        model.addAttribute("user", user);
        model.addAttribute("roles", adminService.getAllRoles());
        return "admin/user-detail";
    }

    @PostMapping("/{userId}/update")
    public String updateUser(
            @PathVariable Long userId,
            UserUpdateRequest request
    ) {
        try {
            adminService.updateUser(userId, request);
            return "redirect:/api/admin/users/" + userId + "?success=true";
        } catch (RuntimeException e) {
            return "redirect:/api/admin/users/" + userId + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/{userId}/delete")
    public String deleteUser(
            @PathVariable Long userId
    ) {
        try {
            adminService.deleteUser(userId);
            return "redirect:/api/admin/users?deleted=true";
        } catch (RuntimeException e) {
            return "redirect:/api/admin/users/" + userId + "?error=" + e.getMessage();
        }
    }
}
