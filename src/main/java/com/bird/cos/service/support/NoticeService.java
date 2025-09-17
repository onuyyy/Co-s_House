package com.bird.cos.service.support;

import com.bird.cos.domain.support.Notice;
import com.bird.cos.dto.support.NoticeResponse;
import com.bird.cos.repository.support.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    //공지사항 전체 조회
    public Page<NoticeResponse> getAllNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable)
                .map(NoticeResponse::fromResponse); // Page.map() 사용 가능
    }

    //공지사항 단건 조회
    public NoticeResponse getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));
        return NoticeResponse.fromResponse(notice);
    }
}
