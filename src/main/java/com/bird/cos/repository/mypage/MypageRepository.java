package com.bird.cos.repository.mypage;

import com.bird.cos.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MypageRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findUserForMyPage(@Param("userId") Long userId);

    @Query("SELECT u FROM User u WHERE u.userEmail = :userEmail")
    Optional<User> findUserForMyPageByEmail(@Param("userEmail") String userEmail);

}