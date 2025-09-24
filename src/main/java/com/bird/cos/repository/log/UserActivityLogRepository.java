package com.bird.cos.repository.log;

import com.bird.cos.domain.log.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    Page<UserActivityLog> findByActivityTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<UserActivityLog> findByUserId_userNameContainingAndActivityTimeBetween(String userName, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

}
