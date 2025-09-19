package com.bird.cos.service.support;

import com.bird.cos.domain.support.Notice;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.support.NoticeRequest;
import com.bird.cos.dto.support.NoticeResponse;
import com.bird.cos.repository.support.NoticeRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    // 공지 생성
    public NoticeResponse createNotice(NoticeRequest request, Long writerId) {
        User writer = userRepository.findById(writerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 제목과 내용 검증
        validateNoticeRequest(request);

        Notice notice = Notice.builder()
                .writer(writer)
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .noticeCreateDate(LocalDateTime.now())
                .build();

        return NoticeResponse.fromResponse(noticeRepository.save(notice));
    }

    // 공지 수정
    public NoticeResponse updateNotice(Long noticeId, NoticeRequest request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항이 없습니다."));

        validateNoticeRequest(request);
        notice.update(request.getTitle().trim(), request.getContent().trim());
        return NoticeResponse.fromResponse(noticeRepository.save(notice));
    }

    // 공지 삭제 (존재 여부 확인)
    public void deleteNotice(Long noticeId) {
        if (!noticeRepository.existsById(noticeId)) {
            throw new IllegalArgumentException("존재하지 않는 공지사항입니다.");
        }
        noticeRepository.deleteById(noticeId);
    }

    // 단건 조회 (조회수 증가)
    @Transactional
    public NoticeResponse getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));

        noticeRepository.save(notice);
        return NoticeResponse.fromResponse(notice);
    }

    // 공지 목록 조회
    @Transactional(readOnly = true)
    public Page<NoticeResponse> getAllNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable)
                .map(NoticeResponse::fromResponse);
    }

    // 제목으로 검색
    @Transactional(readOnly = true)
    public Page<NoticeResponse> searchNoticesByTitle(String title, Pageable pageable) {
        return noticeRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(NoticeResponse::fromResponse);
    }

    // 내용으로 검색
    @Transactional(readOnly = true)
    public Page<NoticeResponse> searchNoticesByContent(String content, Pageable pageable) {
        return noticeRepository.findByContentContainingIgnoreCase(content, pageable)
                .map(NoticeResponse::fromResponse);
    }

    // 전체 검색 (제목 + 내용)
    @Transactional(readOnly = true)
    public Page<NoticeResponse> searchNotices(String keyword, Pageable pageable) {
        return noticeRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                        keyword, keyword, pageable)
                .map(NoticeResponse::fromResponse);
    }


    // 검증 로직
    private void validateNoticeRequest(NoticeRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수 입력 항목입니다.");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수 입력 항목입니다.");
        }
        if (request.getTitle().length() > 200) {
            throw new IllegalArgumentException("제목은 200자 이내로 입력해주세요.");
        }
        if (request.getContent().length() > 5000) {
            throw new IllegalArgumentException("내용은 5000자 이내로 입력해주세요.");
        }
    }
}
