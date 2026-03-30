package com.bird.cos.repository.user;

import com.bird.cos.domain.user.UserPoint;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPointRepository extends JpaRepository<UserPoint, Long>, PointRepositoryCustom {

    /**
     * 사용자의 포인트 엔티티 조회
     */
    Optional<UserPoint> findByUser_UserId(Long userId);

    /**
     * 사용자 포인트 엔티티 비관적 락 조회
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT up FROM UserPoint up WHERE up.user.userId = :userId")
    Optional<UserPoint> findByUserIdForUpdate(@Param("userId") Long userId);

    /**
     * 사용자의 현재 사용 가능한 포인트 조회 (사용자가 없으면 0 반환)
     */
    @Query("SELECT COALESCE((SELECT up.availablePoint FROM UserPoint up WHERE up.user.userId = :userId), 0)")
    Integer getAvailablePointByUserId(@Param("userId") Long userId);

}
