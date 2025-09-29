package com.bird.cos.repository.user;

import com.bird.cos.domain.user.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

    /**
     * 사용자의 포인트 엔티티 조회
     */
    Optional<UserPoint> findByUser_UserId(Long userId);

    /**
     * 사용자의 현재 사용 가능한 포인트 조회 (사용자가 없으면 0 반환)
     */
    @Query("SELECT COALESCE((SELECT up.availablePoint FROM UserPoint up WHERE up.user.userId = :userId), 0)")
    Integer getAvailablePointByUserId(@Param("userId") Long userId);

}