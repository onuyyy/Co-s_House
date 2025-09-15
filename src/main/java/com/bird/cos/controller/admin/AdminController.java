package com.bird.cos.controller.admin;

import com.bird.cos.dto.admin.AdminUserResponse;
import com.bird.cos.dto.admin.AdminUserSearchType;
import com.bird.cos.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@RequestMapping("/admin")
@Controller
public class AdminController {

    private final AdminService adminService;

    @RequestMapping("/main")
    public String adminMainPage() {
        return "forward:/admin/user/";
    }

    @GetMapping("/user")
    public String adminUserPage(
            @RequestParam(required = false, defaultValue = "NAME") AdminUserSearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 20, sort = "userName", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<AdminUserResponse> userList =
                adminService.getUserList(searchType, searchValue, pageable);

        model.addAttribute("userList", userList);
        return "admin/user-list";
    }

}
