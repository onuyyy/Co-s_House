package com.bird.cos.dto.post;

import lombok.Getter;

@Getter
public class WriteCommentRequest {
    private String content;
    private Long postId;
    private Long parentCommentId;
}
