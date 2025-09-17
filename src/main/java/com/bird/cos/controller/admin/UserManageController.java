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
            @RequestParam(required = false, defaultValue = "NAME") UserManageSearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 20, sort = "userName", direction = Sort.Direction.ASC) Pageable pageable,
            Model model
    ) {
        Page<UserManageResponse> userList =
                adminService.getAllUsers(searchType, searchValue, pageable);

        model.addAttribute("userList", userList);
        return "admin/user-list";
    }

    @GetMapping("/role/{roleName}")
    public String usersByRolePage(
            @PathVariable String roleName,
            @RequestParam(required = false, defaultValue = "NAME") UserManageSearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 20, sort = "userName", direction = Sort.Direction.ASC) Pageable pageable,
            Model model
    ) {
        Page<UserManageResponse> userList =
                adminService.getUsersByRole(roleName, searchType, searchValue, pageable);

        model.addAttribute("userList", userList);
        model.addAttribute("roleFilter", roleName);
        
        String pageTitle = switch (roleName) {
            case "USER" -> "일반 사용자 관리";
            case "ADMIN" -> "관리자 관리";
            case "SUPER_ADMIN" -> "슈퍼 관리자 관리";
            default -> "사용자 관리";
        };
        model.addAttribute("pageTitle", pageTitle);
        
        return "admin/user-list";
    }

    @GetMapping("/{userId}")
    public String userDetailPage(
            @PathVariable Long userId, Model model
    ) {
        UserManageResponse user = adminService.getUserDetail(userId);
        model.addAttribute("user", user);
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
