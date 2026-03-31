package com.bird.cos.dto.point;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointUseTestResponse {

    private final boolean success;
    private final Long userId;
    private final Integer usedAmount;
    private final Integer remainingPoints;
    private final String referenceId;
    private final String referenceType;
    private final String message;
}
