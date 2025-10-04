package com.bird.cos.dto.post;

import lombok.Data;

@Data
public class PostSearchRequest {
    private String housingType;
    private Integer roomCount;
    private String familyType;
    private Integer familyCount;
    private Boolean hasPet;
    private String projectType;
    private Boolean isPublic;
}
