package com.bird.cos.service.support;

import com.bird.cos.domain.admin.Admin;
import com.bird.cos.domain.support.Notice;
import com.bird.cos.dto.support.AdminNoticeRequest;
import com.bird.cos.dto.support.NoticeResponse;
import com.bird.cos.repository.support.AdminNoticeRepository;
import com.bird.cos.repository.support.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;
    private final AdminNoticeRepository adminNoticeRepository;

    //공지사항 생성
    public NoticeResponse createNotice(AdminNoticeRequest request) {

        Admin admin = adminNoticeRepository.findById(request.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("관리자 없음")).getAdminId();
        Notice notice = Notice.builder()
                .adminId(admin)
                .title(request.getTitle())
                .content(request.getContent())
                .noticeCreateDate(LocalDateTime.now())
                .build();
        Notice savedNotice = noticeRepository.save(notice);
        return NoticeResponse.fromResponse(savedNotice);
    }

    //공지사항 수정
    public NoticeResponse updateNotice(Long id, AdminNoticeRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지 없음"));
        notice.update(request.getTitle(), request.getContent());
        return NoticeResponse.fromResponse(notice);
    }

    //공지사항 삭제
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }
}
