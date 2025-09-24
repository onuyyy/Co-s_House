package com.bird.cos.dto.support;

import com.bird.cos.domain.support.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class NoticeResponse {
    private Long noticeId;
    private String writerName;
    private String title;
    private String content;
    private LocalDateTime createTime;
    private  LocalDateTime updateTime;

    public static NoticeResponse fromResponse(Notice notice) {
        return NoticeResponse.builder()
                .noticeId(notice.getNoticeId())
                .writerName(notice.getWriter() != null
                        ? notice.getWriter().getUserNickname()
                        : "알 수 없음")
                .title(notice.getTitle())
                .content(notice.getContent())
                .createTime(notice.getNoticeCreateDate())
                .updateTime(notice.getNoticeUpdateDate())
                .build();
    }
}