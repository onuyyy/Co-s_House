package com.bird.cos.repository.user;

import com.bird.cos.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findUsersByUserNameContainingIgnoreCase(String username, Pageable pageable);
    Page<User> findUsersByUserEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<User> findUsersByUserNicknameContainingIgnoreCase(String nickname, Pageable pageable);
    Page<User> findUsersByUserAddressContainingIgnoreCase(String address, Pageable pageable);
    Page<User> findUsersByUserPhoneContainingIgnoreCase(String phone, Pageable pageable);
}
