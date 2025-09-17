package com.bird.cos.dto.support;

import com.bird.cos.domain.admin.Admin;
import com.bird.cos.domain.support.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class NoticeResponse {
    private Long noticeId;
    private Admin adminId;
    private String title;
    private String content;
    private LocalDateTime createTime;
    private  LocalDateTime updateTime;

    public static NoticeResponse fromResponse(Notice notice) {
        return NoticeResponse.builder()
                .noticeId(notice.getNoticeId())
                .adminId(notice.getAdminId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createTime(notice.getNoticeCreateDate())
                .updateTime(notice.getNoticeUpdateDate())
                .build();
    }
}
