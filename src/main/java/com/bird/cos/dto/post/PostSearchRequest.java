package com.bird.cos.dto.post;

import lombok.Data;

@Data
public class PostSearchRequest {
    private String housingType;
    private String areaSize;
    private String roomCount;
    private String familyType;
    private Boolean hasPet;
    private Integer familyCount;
    private String projectType;
    private Boolean isPublic;
}
