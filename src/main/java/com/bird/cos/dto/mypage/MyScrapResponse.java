package com.bird.cos.dto.mypage;

import com.bird.cos.domain.scrap.Scrap;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MyScrapResponse {
    private Long scrapId;
    private Long postId;
    private String imageUrl;
    private String title;

    public static MyScrapResponse from(Scrap scrap){
        return MyScrapResponse.builder()
                .scrapId(scrap.getScrapId())
                .postId(scrap.getPost().getPostId())
                .imageUrl(scrap.getPost().getThumbnailUrl())
                .title(scrap.getPost().getTitle())
                .build();
    }
}
