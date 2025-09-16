package com.bird.cos.repository.admin;

import com.bird.cos.domain.admin.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Page<Admin> findAdminsByAdminNameContainingIgnoreCase(String username, Pageable pageable);
    Page<Admin> findAdminsByAdminEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<Admin> findAdminsByAdminPhoneContainingIgnoreCase(String phone, Pageable pageable);
    Page<Admin> findAdminsByAdminRole_AdminRoleNameContainingIgnoreCase(String role, Pageable pageable);

}
