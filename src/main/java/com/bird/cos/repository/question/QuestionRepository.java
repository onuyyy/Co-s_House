// *1. JpaRepository 상속 추가, JOIN FETCH 쿼리로 User/CommonCode 함께 조회
package com.bird.cos.repository.question;

import com.bird.cos.domain.proudct.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    //페이징 - 모든 문의 조회 (간단한 기본 메서드)
    @Query("SELECT q FROM Question q ORDER BY q.questionCreatedAt DESC")
    Page<Question> findAllWithUser(Pageable pageable);

    //페이징 - 특정 사용자 문의 조회 (간단한 기본 메서드)
    @Query("SELECT q FROM Question q WHERE q.user.userId = :userId ORDER BY q.questionCreatedAt DESC")
    Page<Question> findAllByUserIdWithUser(@Param("userId") Long userId, Pageable pageable);
}