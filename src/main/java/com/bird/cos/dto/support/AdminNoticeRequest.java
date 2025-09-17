package com.bird.cos.dto.support;

import com.bird.cos.domain.admin.Admin;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor // 기본 생성자
public class AdminNoticeRequest {
    private Long noticeId;
    private Long adminId;
    private String title;
    private String content;

}
