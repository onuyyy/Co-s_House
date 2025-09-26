package com.bird.cos.repository.log;

import com.bird.cos.domain.log.UserActivityLog;
import com.bird.cos.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    Page<UserActivityLog> findByActivityTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<UserActivityLog> findByUserId_userNameContainingAndActivityTimeBetween(String userName, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Modifying
    @Query("DELETE FROM UserActivityLog ual WHERE ual.userId = :user")
    void deleteByUserId(@Param("user") User user);

}
