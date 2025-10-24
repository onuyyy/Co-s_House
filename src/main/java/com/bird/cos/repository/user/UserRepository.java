package com.bird.cos.repository.user;

import com.bird.cos.domain.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.userId = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    // 기본 사용자 검색 메서드들
    Page<User> findUsersByUserNameContainingIgnoreCase(String username, Pageable pageable);
    Page<User> findUsersByUserEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<User> findUsersByUserNicknameContainingIgnoreCase(String nickname, Pageable pageable);
    Page<User> findUsersByUserPhoneContainingIgnoreCase(String phone, Pageable pageable);

    // 역할별 사용자 검색
    @Query("SELECT u FROM User u WHERE u.userRole.userRoleName = :roleName")
    Page<User> findUsersByRoleName(@Param("roleName") String roleName, Pageable pageable);

    // 역할별 + 이름 검색
    @Query("SELECT u FROM User u WHERE u.userRole.userRoleName = :roleName AND u.userName LIKE %:userName%")
    Page<User> findUsersByRoleNameAndUserNameContaining(@Param("roleName") String roleName, 
                                                        @Param("userName") String userName, 
                                                        Pageable pageable);

    // 역할별 + 이메일 검색
    @Query("SELECT u FROM User u WHERE u.userRole.userRoleName = :roleName AND u.userEmail LIKE %:userEmail%")
    Page<User> findUsersByRoleNameAndUserEmailContaining(@Param("roleName") String roleName, 
                                                         @Param("userEmail") String userEmail, 
                                                         Pageable pageable);

    // 역할별 + 닉네임 검색
    @Query("SELECT u FROM User u WHERE u.userRole.userRoleName = :roleName AND u.userNickname LIKE %:userNickname%")
    Page<User> findUsersByRoleNameAndUserNicknameContaining(@Param("roleName") String roleName, 
                                                            @Param("userNickname") String userNickname, 
                                                            Pageable pageable);

    // 역할별 + 연락처 검색
    @Query("SELECT u FROM User u WHERE u.userRole.userRoleName = :roleName AND u.userPhone LIKE %:userPhone%")
    Page<User> findUsersByRoleNameAndUserPhoneContaining(@Param("roleName") String roleName, 
                                                         @Param("userPhone") String userPhone, 
                                                         Pageable pageable);

    Optional<User> findByUserEmail(String userEmail);
    Optional<User> findByUserNickname(String userNickname);


    @Query("select u from User u left join fetch u.userRole where u.userId = :userId")
    Optional<User> findWithRoleByUserId(@Param("userId") Long userId);

    @Query("select u from User u left join fetch u.userRole where u.userEmail = :userEmail")
    Optional<User> findWithRoleByEmail(@Param("userEmail") String userEmail);

    Optional<User> findBySocialProviderAndSocialId(String socialProvider, String socialId);



    // 중복 체크 (자신 제외)
    boolean existsByUserEmailAndUserIdNot(String userEmail, Long userId);
    boolean existsByUserNicknameAndUserIdNot(String userNickname, Long userId);

    // 관리자(ADMIN, SUPER_ADMIN) 전용 조회 메서드들
    @Query("SELECT u FROM User u WHERE u.userRole.userRoleName IN ('ADMIN', 'SUPER_ADMIN')")
    Page<User> findAdminUsers(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.userRole.userRoleName IN ('ADMIN', 'SUPER_ADMIN') AND u.userName LIKE %:userName%")
    Page<User> findAdminUsersByUserNameContaining(@Param("userName") String userName, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.userRole.userRoleName IN ('ADMIN', 'SUPER_ADMIN') AND u.userEmail LIKE %:userEmail%")
    Page<User> findAdminUsersByUserEmailContaining(@Param("userEmail") String userEmail, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.userRole.userRoleName IN ('ADMIN', 'SUPER_ADMIN') AND u.userNickname LIKE %:userNickname%")
    Page<User> findAdminUsersByUserNicknameContaining(@Param("userNickname") String userNickname, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.userRole.userRoleName IN ('ADMIN', 'SUPER_ADMIN') AND u.userPhone LIKE %:userPhone%")
    Page<User> findAdminUsersByUserPhoneContaining(@Param("userPhone") String userPhone, Pageable pageable);

}
