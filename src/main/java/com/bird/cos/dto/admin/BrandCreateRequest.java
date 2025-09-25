package com.bird.cos.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrandCreateRequest {

    @NotBlank(message = "브랜드 이름은 필수입니다.")
    private String brandName;
    private String logoUrl;
    private String brandDescription;

}
