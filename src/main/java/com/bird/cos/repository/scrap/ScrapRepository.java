package com.bird.cos.repository.scrap;

import com.bird.cos.domain.scrap.Scrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    
    // 특정 사용자와 게시글의 스크랩 조회
    Optional<Scrap> findByUser_UserIdAndPost_PostId(Long userId, Long postId);
    
    // 특정 사용자의 스크랩 여부 확인
    boolean existsByUser_UserIdAndPost_PostId(Long userId, Long postId);
    
    // 특정 게시글의 스크랩 수 조회
    long countByPost_PostId(Long postId);
    
    // 특정 사용자가 스크랩한 게시글 ID 목록 조회 (여러 게시글 한번에)
    @Query("SELECT s.post.postId FROM Scrap s WHERE s.user.userId = :userId AND s.post.postId IN :postIds")
    List<Long> findScrapedPostIdsByUserAndPosts(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    Page<Scrap> findByUser_UserId(Long userId, Pageable pageable);
}
