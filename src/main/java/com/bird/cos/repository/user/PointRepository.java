package com.bird.cos.repository.user;

import com.bird.cos.domain.user.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 기존 포인트 Repository (EventService 등에서 사용)
 */
@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    boolean existsByUser_UserIdAndPointDescription(Long userId, String pointDescription);

    /**
     * 사용자의 총 포인트 잔액 조회
     * 적립 포인트는 양수(+), 사용 포인트는 음수(-)로 저장되어 있다고 가정
     */
    @Query("SELECT COALESCE(SUM(p.pointAmount), 0) FROM Point p WHERE p.user.userId = :userId")
    Integer getTotalPointsByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 포인트 내역 조회 (최신순)
     */
    List<Point> findByUser_UserIdOrderByPointCreatedAtDesc(Long userId);

    /**
     * 사용자의 포인트 내역 조회 (특정 개수 제한)
     */
    @Query("SELECT p FROM Point p WHERE p.user.userId = :userId ORDER BY p.pointCreatedAt DESC")
    List<Point> findTopPointsByUserId(@Param("userId") Long userId, Pageable pageable);
}
