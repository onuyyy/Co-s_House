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
            @PageableDefault(size = 20, sort = "userName", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<UserManageResponse> userList =
                adminService.getUserList(searchType, searchValue, pageable);

        model.addAttribute("userList", userList);
        return "admin/user-list";
    }

    @GetMapping("/{userId}")
    public String adminUserDetailPage(
            @PathVariable Long userId, Model model
    ) {
        model.addAttribute("user", adminService.getUserDetail(userId));

        return "admin/user-detail";
    }

    @PostMapping("/{userId}/update")
    public String adminUserUpdate(
            @PathVariable Long userId,
            UserUpdateRequest request
    ) {

        adminService.updateUser(userId, request);
        return "redirect:/api/admin/users/" + userId;
    }

    @PostMapping("/{userId}/delete")
    public String adminUserDelete(
            @PathVariable Long userId
    ) {
        adminService.deleteUser(userId);

        return "redirect:/api/admin/users";
    }

}
