package com.bird.cos.repository.user;

import com.bird.cos.domain.user.PointHistory;
import com.bird.cos.domain.user.PointType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트 변동 내역 관리 Repository
 * User와 N:1 관계로 모든 포인트 변동 내역 추적
 */
@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    /**
     * 사용자 ID로 포인트 내역 조회 (페이징)
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 포인트 내역 페이지
     */
    Page<PointHistory> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자의 특정 타입 포인트 내역 조회
     * @param userId 사용자 ID
     * @param type 포인트 타입
     * @param pageable 페이징 정보
     * @return 포인트 내역 페이지
     */
    Page<PointHistory> findByUser_UserIdAndTypeOrderByCreatedAtDesc(Long userId, PointType type, Pageable pageable);

    /**
     * 사용자의 특정 기간 포인트 내역 조회
     * @param userId 사용자 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return 포인트 내역 페이지
     */
    Page<PointHistory> findByUser_UserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 사용자의 특정 참조 타입 포인트 내역 조회 (주문, 이벤트 등)
     * @param userId 사용자 ID
     * @param referenceType 참조 타입
     * @param pageable 페이징 정보
     * @return 포인트 내역 페이지
     */
    Page<PointHistory> findByUser_UserIdAndReferenceTypeOrderByCreatedAtDesc(
        Long userId, String referenceType, Pageable pageable);

    /**
     * 참조 ID로 포인트 내역 조회 (주문번호 등)
     * @param referenceId 참조 ID
     * @return 포인트 내역 리스트
     */
    List<PointHistory> findByReferenceIdOrderByCreatedAtDesc(String referenceId);

    /**
     * 사용자의 포인트 타입별 총 금액 조회
     * @param userId 사용자 ID
     * @param type 포인트 타입
     * @return 총 금액 (없으면 0)
     */
    @Query("SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph WHERE ph.user.userId = :userId AND ph.type = :type")
    Integer getTotalAmountByUserAndType(@Param("userId") Long userId, @Param("type") PointType type);

    /**
     * 사용자의 특정 기간 포인트 타입별 총 금액 조회
     * @param userId 사용자 ID
     * @param type 포인트 타입
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 총 금액 (없으면 0)
     */
    @Query("SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph " +
           "WHERE ph.user.userId = :userId AND ph.type = :type " +
           "AND ph.createdAt BETWEEN :startDate AND :endDate")
    Integer getTotalAmountByUserAndTypeAndPeriod(
        @Param("userId") Long userId,
        @Param("type") PointType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 월별 포인트 적립 통계 조회
     * @param userId 사용자 ID
     * @param year 년도
     * @param month 월
     * @return 월별 적립 포인트 합계
     */
    @Query("SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph " +
           "WHERE ph.user.userId = :userId AND ph.type = 'EARN' " +
           "AND YEAR(ph.createdAt) = :year AND MONTH(ph.createdAt) = :month")
    Integer getMonthlyEarnedPoints(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    /**
     * 사용자의 월별 포인트 사용 통계 조회
     * @param userId 사용자 ID
     * @param year 년도
     * @param month 월
     * @return 월별 사용 포인트 합계 (절댓값)
     */
    @Query("SELECT COALESCE(ABS(SUM(ph.amount)), 0) FROM PointHistory ph " +
           "WHERE ph.user.userId = :userId AND ph.type IN ('USE', 'EXPIRE') " +
           "AND YEAR(ph.createdAt) = :year AND MONTH(ph.createdAt) = :month")
    Integer getMonthlyUsedPoints(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    /**
     * 특정 참조 ID와 타입으로 포인트 내역 존재 여부 확인 (중복 방지)
     * @param referenceId 참조 ID
     * @param referenceType 참조 타입
     * @param type 포인트 타입
     * @return 내역 존재 여부
     */
    boolean existsByReferenceIdAndReferenceTypeAndType(String referenceId, String referenceType, PointType type);

    /**
     * 시스템 전체 포인트 내역 개수 조회 (관리자용)
     * @return 전체 포인트 내역 개수
     */
    @Query("SELECT COUNT(ph) FROM PointHistory ph")
    Long getTotalHistoryCount();

    /**
     * 특정 기간 내 포인트 타입별 전체 통계 조회 (관리자용)
     * @param type 포인트 타입
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 해당 기간 포인트 타입별 총 금액
     */
    @Query("SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph " +
           "WHERE ph.type = :type AND ph.createdAt BETWEEN :startDate AND :endDate")
    Long getSystemTotalByTypeAndPeriod(
        @Param("type") PointType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 포인트 내역 삭제 (탈퇴 시)
     * @param userId 사용자 ID
     */
    void deleteByUser_UserId(Long userId);

    /**
     * 특정 기간보다 오래된 포인트 내역 조회 (데이터 정리용)
     * @param cutoffDate 기준일
     * @return 오래된 포인트 내역 리스트
     */
    List<PointHistory> findByCreatedAtBefore(LocalDateTime cutoffDate);
}