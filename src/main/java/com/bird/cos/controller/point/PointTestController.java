package com.bird.cos.controller.point;

import com.bird.cos.dto.point.PointUseTestRequest;
import com.bird.cos.dto.point.PointUseTestResponse;
import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.user.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Profile("test")
@RequiredArgsConstructor
@RequestMapping("/test/points")
public class PointTestController {

    private static final String DEFAULT_DESCRIPTION = "성능 테스트 포인트 차감";
    private static final String DEFAULT_REFERENCE_TYPE = "PERF_TEST";

    private final PointService pointService;

    @PostMapping("/use")
    public PointUseTestResponse usePoints(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @Valid @RequestBody PointUseTestRequest request) {
        return executeUsePoints(userDetails, request, PointCase.C);
    }

    @PostMapping("/use/a")
    public PointUseTestResponse usePointsCaseA(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @Valid @RequestBody PointUseTestRequest request) {
        return executeUsePoints(userDetails, request, PointCase.A);
    }

    @PostMapping("/use/b")
    public PointUseTestResponse usePointsCaseB(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @Valid @RequestBody PointUseTestRequest request) {
        return executeUsePoints(userDetails, request, PointCase.B);
    }

    private PointUseTestResponse executeUsePoints(CustomUserDetails userDetails,
                                                  PointUseTestRequest request,
                                                  PointCase pointCase) {
        Long userId = userDetails.getUserId();
        String referenceId = UUID.randomUUID().toString();
        String description = hasText(request.getDescription()) ? request.getDescription() : DEFAULT_DESCRIPTION;
        String referenceType = hasText(request.getReferenceType())
                ? request.getReferenceType()
                : DEFAULT_REFERENCE_TYPE + "_" + pointCase.name();

        switch (pointCase) {
            case A -> pointService.usePointsCaseA(userId, request.getAmount(), description, referenceId, referenceType);
            case B -> pointService.usePointsCaseB(userId, request.getAmount(), description, referenceId, referenceType);
            case C -> pointService.usePoints(userId, request.getAmount(), description, referenceId, referenceType);
        }

        return PointUseTestResponse.builder()
                .success(true)
                .userId(userId)
                .usedAmount(request.getAmount())
                .remainingPoints(pointService.getAvailablePoints(userId))
                .referenceId(referenceId)
                .referenceType(referenceType)
                .message("포인트 차감이 완료되었습니다.")
                .build();
    }

    private enum PointCase {
        A, B, C
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
