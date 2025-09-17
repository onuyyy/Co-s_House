package com.bird.cos.service.support;

import com.bird.cos.domain.admin.Admin;
import com.bird.cos.domain.support.Notice;
import com.bird.cos.dto.support.AdminNoticeRequest;
import com.bird.cos.dto.support.NoticeResponse;
import com.bird.cos.repository.support.AdminNoticeRepository;
import com.bird.cos.repository.support.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AdminNoticeServiceTest {

    private AdminNoticeService adminNoticeService;
    private NoticeRepository noticeRepository;
    private AdminNoticeRepository adminNoticeRepository;

    @BeforeEach
    void setUp() {
        noticeRepository = mock(NoticeRepository.class);
        adminNoticeRepository = mock(AdminNoticeRepository.class);
        adminNoticeService = new AdminNoticeService(noticeRepository, adminNoticeRepository);
    }

    @Test
    void createNotice() {
        // given
//        Admin admin = Admin.builder().adminId(1L).adminName("관리자").build();
        AdminNoticeRequest request = new AdminNoticeRequest();
        request.setAdminId(1L);
        request.setTitle("테스트 공지");
        request.setContent("공지 내용");

        ArgumentCaptor<Notice> noticeCaptor = ArgumentCaptor.forClass(Notice.class);
        when(noticeRepository.save(noticeCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        NoticeResponse response = adminNoticeService.createNotice(request);

        // then
        Notice savedNotice = noticeCaptor.getValue();
        assertThat(savedNotice.getTitle()).isEqualTo("테스트 공지");
        assertThat(savedNotice.getContent()).isEqualTo("공지 내용");

        assertThat(response.getTitle()).isEqualTo("테스트 공지");
        assertThat(response.getContent()).isEqualTo("공지 내용");

        verify(adminNoticeRepository, times(1)).findById(1L);
        verify(noticeRepository, times(1)).save(any(Notice.class));
    }

    @Test
    void createNotice_1() { //관리자 없을 때
        AdminNoticeRequest request = new AdminNoticeRequest();
        request.setAdminId(999L);
        request.setTitle("테스트 공지");
        request.setContent("공지 내용");

        when(adminNoticeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminNoticeService.createNotice(request));

        verify(adminNoticeRepository, times(1)).findById(999L);
        verifyNoInteractions(noticeRepository);
    }

    @Test
    void updateNotice(){
        Notice notice = Notice.builder()
                .noticeId(1L)
                .title("기존 제목")
                .content("기존 내용")
                .noticeCreateDate(LocalDateTime.now())
                .build();

        AdminNoticeRequest request = new AdminNoticeRequest();
        request.setTitle("수정 제목");
        request.setContent("수정 내용");

        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        NoticeResponse response = adminNoticeService.updateNotice(1L, request);

        assertThat(notice.getTitle()).isEqualTo("수정 제목");
        assertThat(notice.getContent()).isEqualTo("수정 내용");

        assertThat(response.getTitle()).isEqualTo("수정 제목");
        assertThat(response.getContent()).isEqualTo("수정 내용");

        verify(noticeRepository, times(1)).findById(1L);
    }

    @Test
    void updateNotice_1() {
        AdminNoticeRequest request = new AdminNoticeRequest();
        request.setTitle("수정 제목");
        request.setContent("수정 내용");

        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminNoticeService.updateNotice(999L, request));

        verify(noticeRepository, times(1)).findById(999L);
    }

    @Test
    void deleteNotice() {
        doNothing().when(noticeRepository).deleteById(1L);

        adminNoticeService.deleteNotice(1L);

        verify(noticeRepository, times(1)).deleteById(1L);
    }
}
