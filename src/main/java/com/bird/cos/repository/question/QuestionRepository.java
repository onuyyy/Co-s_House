// *1. JpaRepository 상속 추가, JOIN FETCH 쿼리로 User/CommonCode 함께 조회
package com.bird.cos.repository.question;

import com.bird.cos.domain.product.Question;
import com.bird.cos.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    long countByProduct_ProductId(Long productId);

    //페이징 - 특정 사용자 문의 조회
    @Query("SELECT q FROM Question q WHERE q.user.userId = :userId ORDER BY q.questionCreatedAt DESC")
    Page<Question> findAllByUserIdWithUser(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Question q SET q.user = null WHERE q.user.userId = :userId")
    void anonymizeQuestionsByUser(@Param("userId") Long userId);

    @Query("SELECT q FROM Question q WHERE q.product.id = :productId ORDER BY q.questionCreatedAt DESC")
    Page<Question> findQuestionsByProductId(@Param("productId") Long productId, Pageable pageable);
}