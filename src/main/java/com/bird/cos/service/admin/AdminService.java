package com.bird.cos.service.admin;

import com.bird.cos.domain.user.User;
import com.bird.cos.dto.admin.AdminUserResponse;
import com.bird.cos.dto.admin.AdminUserSearchType;
import com.bird.cos.dto.admin.UserDetailResponse;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final UserRepository userRepository;

    public Page<AdminUserResponse> getUserList(
            AdminUserSearchType searchType, String searchValue, Pageable pageable)
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

        return users.map(AdminUserResponse::from);
    }

    public UserDetailResponse getUserDetail(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return UserDetailResponse.from(user);
    }
}
