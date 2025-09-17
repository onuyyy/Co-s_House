package com.bird.cos.service.support;

import com.bird.cos.domain.support.Notice;
import com.bird.cos.dto.support.NoticeResponse;
import com.bird.cos.repository.support.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class NoticeServiceTest {

    private NoticeService noticeService;
    private NoticeRepository noticeRepository;

    @BeforeEach
    void setUp() {
        noticeRepository = mock(NoticeRepository.class);
        noticeService = new NoticeService(noticeRepository);
    }

    @Test
    void getAllNotices() {//정상
        // given
        Notice notice1 = Notice.builder()
                .noticeId(1L)
                .title("공지1")
                .content("내용1")
                .noticeCreateDate(LocalDateTime.now())
                .build();
        Notice notice2 = Notice.builder()
                .noticeId(2L)
                .title("공지2")
                .content("내용2")
                .noticeCreateDate(LocalDateTime.now())
                .build();

        when(noticeRepository.findAll()).thenReturn(Arrays.asList(notice1, notice2));

        // when
        List<NoticeResponse> responses = noticeService.getAllNotices();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo("공지1");
        assertThat(responses.get(1).getTitle()).isEqualTo("공지2");

        verify(noticeRepository, times(1)).findAll();
    }

    @Test
    void getNotice() {//정상
        // given
        Notice notice = Notice.builder()
                .noticeId(1L)
                .title("공지1")
                .content("내용1")
                .noticeCreateDate(LocalDateTime.now())
                .build();

        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        // when
        NoticeResponse response = noticeService.getNotice(1L);

        // then
        assertThat(response.getTitle()).isEqualTo("공지1");
        assertThat(response.getContent()).isEqualTo("내용1");

        verify(noticeRepository, times(1)).findById(1L);
    }

    @Test
    void getNotice1() {//예외_존재하지 않음
        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> noticeService.getNotice(999L));

        verify(noticeRepository, times(1)).findById(999L);
    }
}
