package com.bird.cos.repository.support;

import com.bird.cos.domain.support.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 제목으로 검색
    Page<Notice> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 내용으로 검색
    Page<Notice> findByContentContainingIgnoreCase(String content, Pageable pageable);

    // 제목 또는 내용으로 검색
    Page<Notice> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);
}
