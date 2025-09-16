package com.bird.cos.service.admin;

import com.bird.cos.domain.admin.Admin;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.admin.*;
import com.bird.cos.repository.admin.AdminRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Transactional(readOnly = true)
    public Page<UserManageResponse> getUserList(
            UserManageSearchType searchType, String searchValue, Pageable pageable)
    {
        Page<User> users;

        if (searchValue == null || searchValue.isEmpty()) {
            users = userRepository.findAll(pageable);
        } else {
            users = switch (searchType) {
                case NAME -> userRepository.findUsersByUserNameContainingIgnoreCase(searchValue, pageable);
                case EMAIL -> userRepository.findUsersByUserEmailContainingIgnoreCase(searchValue, pageable);
                case NICKNAME -> userRepository.findUsersByUserNicknameContainingIgnoreCase(searchValue, pageable);
                case ADDRESS -> userRepository.findUsersByUserAddressContainingIgnoreCase(searchValue, pageable);
                case PHONE -> userRepository.findUsersByUserPhoneContainingIgnoreCase(searchValue, pageable);
            };
        }

        return users.map(UserManageResponse::from);
    }

    @Transactional(readOnly = true)
    public UserManageResponse getUserDetail(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return UserManageResponse.from(user);
    }

    public void updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.update(request);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("삭제할 사용자가 없습니다.");
        }
        userRepository.deleteById(userId);
    }

    public Page<AdminManageResponse> getAdminList(
            AdminManagerSearchType searchType, String searchValue, Pageable pageable)
    {
        Page<Admin> admins;

        if (searchValue == null || searchValue.isEmpty()) {
            admins = adminRepository.findAll(pageable);
        } else {
            admins = switch (searchType) {
                case NAME -> adminRepository.findAdminsByAdminNameContainingIgnoreCase(searchValue, pageable);
                case EMAIL -> adminRepository.findAdminsByAdminEmailContainingIgnoreCase(searchValue, pageable);
                case PHONE -> adminRepository.findAdminsByAdminPhoneContainingIgnoreCase(searchValue, pageable);
                case ROLE -> adminRepository.findAdminsByAdminRole_AdminRoleNameContainingIgnoreCase(searchValue, pageable);
            };
        }

        return admins.map(AdminManageResponse::from);
    }

    public AdminManageResponse getAdminDetail(Long adminId) {

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        return AdminManageResponse.from(admin);
    }

    public void updateAdmin(Long adminId, AdminUpdateRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        admin.update(request);
    }

    public void deleteAdmin(Long adminId) {
        if (!adminRepository.existsById(adminId)) {
            throw new RuntimeException("삭제할 관리자가 없습니다.");
        }
        adminRepository.deleteById(adminId);
    }
}
