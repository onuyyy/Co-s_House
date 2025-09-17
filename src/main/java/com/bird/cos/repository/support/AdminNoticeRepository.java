package com.bird.cos.repository.support;

import com.bird.cos.domain.support.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminNoticeRepository extends JpaRepository<Notice,Long> {
}
