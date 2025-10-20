package com.bird.cos.service.support;

import com.bird.cos.domain.support.Notice;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.support.NoticeRequest;
import com.bird.cos.dto.support.NoticeResponse;
import com.bird.cos.repository.support.NoticeRepository;
import com.bird.cos.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoticeService noticeService;

    private User writer;
    private NoticeRequest request;

    @BeforeEach
    void setUp() {
        writer = User.builder()
                .userId(1L)
                .userEmail("admin@example.com")
                .userNickname("관리자")
                .userName("관리자")
                .build();

        request = NoticeRequest.builder()
                .title("공지 제목")
                .content("공지 내용")
                .build();
    }

    // 정상 요청 시 공지가 생성되고 저장 결과가 응답으로 변환되는지 검증
    @Test
    void createNotice_WhenValidRequest_SavesNoticeAndReturnsResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(writer));
        when(noticeRepository.save(any(Notice.class))).thenAnswer(invocation -> {
            Notice saved = invocation.getArgument(0);
            return Notice.builder()
                    .noticeId(10L)
                    .writer(saved.getWriter())
                    .title(saved.getTitle())
                    .content(saved.getContent())
                    .noticeCreateDate(saved.getNoticeCreateDate())
                    .build();
        });

        NoticeResponse response = noticeService.createNotice(request, 1L);

        assertThat(response.getNoticeId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("공지 제목");
        assertThat(response.getWriterName()).isEqualTo("관리자");

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeRepository).save(captor.capture());
        Notice saved = captor.getValue();
        assertThat(saved.getWriter()).isEqualTo(writer);
        assertThat(saved.getNoticeCreateDate()).isNotNull();
    }

    // 작성자를 찾을 수 없으면 공지 생성이 실패하는지 검증
    @Test
    void createNotice_WhenWriterNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.createNotice(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    // 제목이 비어 있을 때 유효성 검증에서 예외가 발생하는지 검증
    @Test
    void createNotice_WhenTitleBlank_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(writer));
        request.setTitle("   ");

        assertThatThrownBy(() -> noticeService.createNotice(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 필수 입력 항목입니다.");
    }

    // 공지 수정 시 존재하는 공지를 찾아 제목과 내용을 업데이트하는지 검증
    @Test
    void updateNotice_WhenNoticeExists_UpdatesAndSaves() {
        Notice notice = Notice.builder()
                .noticeId(5L)
                .writer(writer)
                .title("이전 제목")
                .content("이전 내용")
                .noticeCreateDate(LocalDateTime.now().minusDays(1))
                .build();

        when(noticeRepository.findById(5L)).thenReturn(Optional.of(notice));
        when(noticeRepository.save(notice)).thenReturn(notice);

        noticeService.updateNotice(5L, request);

        assertThat(notice.getTitle()).isEqualTo("공지 제목");
        assertThat(notice.getContent()).isEqualTo("공지 내용");
        assertThat(notice.getNoticeUpdateDate()).isNotNull();
        verify(noticeRepository).save(notice);
    }

    // 수정 대상 공지를 찾을 수 없으면 예외를 던지는지 검증
    @Test
    void updateNotice_WhenNoticeMissing_ThrowsException() {
        when(noticeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.updateNotice(99L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("공지사항이 없습니다.");
    }

    // 삭제 대상 공지가 존재할 때 정상적으로 삭제가 수행되는지 검증
    @Test
    void deleteNotice_WhenExists_DeletesById() {
        when(noticeRepository.existsById(7L)).thenReturn(true);

        noticeService.deleteNotice(7L);

        verify(noticeRepository).deleteById(7L);
    }

    // 삭제 대상 공지가 없을 때 예외를 던지는지 검증
    @Test
    void deleteNotice_WhenMissing_ThrowsException() {
        when(noticeRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> noticeService.deleteNotice(7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 공지사항입니다.");
    }

    // 단건 조회 시 공지를 찾아 응답으로 매핑하는지 검증
    @Test
    void getNotice_WhenExists_ReturnsResponse() {
        Notice notice = Notice.builder()
                .noticeId(3L)
                .writer(writer)
                .title("제목")
                .content("내용")
                .noticeCreateDate(LocalDateTime.now())
                .build();
        when(noticeRepository.findById(3L)).thenReturn(Optional.of(notice));
        when(noticeRepository.save(notice)).thenReturn(notice);

        NoticeResponse response = noticeService.getNotice(3L);

        assertThat(response.getNoticeId()).isEqualTo(3L);
        assertThat(response.getTitle()).isEqualTo("제목");
        verify(noticeRepository).save(notice);
    }

    // 단건 조회 시 공지를 찾지 못하면 예외를 던지는지 검증
    @Test
    void getNotice_WhenNotFound_ThrowsException() {
        when(noticeRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.getNotice(3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 공지사항입니다.");
    }

    // 공지 목록 조회가 페이지 단위로 응답되는지 검증
    @Test
    void getAllNotices_ReturnsPagedResponses() {
        Notice notice = Notice.builder()
                .noticeId(1L)
                .writer(writer)
                .title("안내")
                .content("내용")
                .noticeCreateDate(LocalDateTime.now())
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        when(noticeRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(notice), pageable, 1));

        Page<NoticeResponse> page = noticeService.getAllNotices(pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("안내");
    }

    // 제목 검색이 리포지터리 로직을 통해 수행되는지 검증
    @Test
    void searchNoticesByTitle_ReturnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Notice notice = Notice.builder()
                .noticeId(2L)
                .writer(writer)
                .title("제목 공지")
                .content("내용")
                .noticeCreateDate(LocalDateTime.now())
                .build();
        when(noticeRepository.findByTitleContainingIgnoreCase("제목", pageable))
                .thenReturn(new PageImpl<>(List.of(notice), pageable, 1));

        Page<NoticeResponse> page = noticeService.searchNoticesByTitle("제목", pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getTitle()).contains("제목");
    }

    // 내용 검색이 리포지터리 로직을 통해 수행되는지 검증
    @Test
    void searchNoticesByContent_ReturnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Notice notice = Notice.builder()
                .noticeId(2L)
                .writer(writer)
                .title("제목")
                .content("찾는 내용")
                .noticeCreateDate(LocalDateTime.now())
                .build();
        when(noticeRepository.findByContentContainingIgnoreCase("내용", pageable))
                .thenReturn(new PageImpl<>(List.of(notice), pageable, 1));

        Page<NoticeResponse> page = noticeService.searchNoticesByContent("내용", pageable);

        assertThat(page.getContent().get(0).getContent()).contains("내용");
    }

    // 제목 또는 내용 검색이 모두 동작하는지 검증
    @Test
    void searchNotices_ReturnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Notice notice = Notice.builder()
                .noticeId(4L)
                .writer(writer)
                .title("키워드 공지")
                .content("본문")
                .noticeCreateDate(LocalDateTime.now())
                .build();
        when(noticeRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase("키워드", "키워드", pageable))
                .thenReturn(new PageImpl<>(List.of(notice), pageable, 1));

        Page<NoticeResponse> page = noticeService.searchNotices("키워드", pageable);

        assertThat(page.getContent().get(0).getTitle()).contains("키워드");
    }
}

