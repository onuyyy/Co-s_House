package com.bird.cos.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BrandUpdateRequest {
    
    private String brandName;
    private String logoUrl;
    private String brandDescription;
}
