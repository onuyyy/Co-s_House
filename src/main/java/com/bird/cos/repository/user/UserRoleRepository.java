package com.bird.cos.repository.user;

import com.bird.cos.domain.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    Optional<UserRole> findByUserRoleName(String userRoleName);

    Optional<UserRole> findFirstByUserRoleNameOrderByUserRoleIdAsc(String userRoleName);
    
    boolean existsByUserRoleName(String userRoleName);
}
