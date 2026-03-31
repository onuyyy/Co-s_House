package com.bird.cos.dto.point;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointUseTestRequest {

    @NotNull
    @Min(1)
    private Integer amount;

    private String description;

    private String referenceType;
}
