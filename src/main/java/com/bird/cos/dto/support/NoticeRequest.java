package com.bird.cos.dto.support;

import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeRequest {
    private Long writerId;
    private String title;
    private String content;
}