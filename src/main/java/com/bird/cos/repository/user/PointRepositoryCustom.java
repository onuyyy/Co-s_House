package com.bird.cos.repository.user;

import com.bird.cos.domain.user.PointHistory;
import com.bird.cos.dto.mypage.MyPointRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointRepositoryCustom {
    Page<PointHistory> searchPointHistory(Long userId, MyPointRequest request, Pageable pageable);
}
