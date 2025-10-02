package com.bird.cos.dto.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewImage {
    private Long id;
    private String url;

    public ReviewImage(Long id, String url) {
        this.id = id;
        this.url = url;
    }

}
