package com.bird.cos.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    private BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    private BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public static BusinessException productNotFound(Long productId) {
        return new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                "상품을 찾을 수 없습니다: " + productId);
    }

    public static BusinessException orderNotFound(Long orderId) {
        return new BusinessException(ErrorCode.ORDER_NOT_FOUND,
                "주문을 찾을 수 없습니다: " + orderId);
    }

    public static BusinessException orderAccessDenied() {
        return new BusinessException(ErrorCode.ORDER_ACCESS_DENIED,
                "주문에 접근할 수 있는 권한이 없습니다.");
    }

    public static BusinessException couponNotFound(Long couponId) {
        return new BusinessException(ErrorCode.COUPON_NOT_FOUND,
                "쿠폰을 찾을 수 없습니다." + couponId);
    }

    public static BusinessException couponExpired(Long couponId) {
        return new BusinessException(ErrorCode.COUPON_EXPIRED,
                "만료되었거나 비활성화된 쿠폰입니다." + couponId);
    }

    public static BusinessException couponUsed(Long couponId) {
        return new BusinessException(ErrorCode.COUPON_USED,
                "이미 사용된 쿠폰입니다." + couponId);
    }

    public static BusinessException userNotFound() {
        return new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    public static BusinessException codeNotFound() {
        return new BusinessException(ErrorCode.CODE_NOT_FOUND);
    }

    public static BusinessException optionNotFound() {
        return new BusinessException(ErrorCode.OPTION_NOT_FOUND);
    }

    public static BusinessException optionBadRequest() {
        return new BusinessException(ErrorCode.OPTION_BAD_REQUEST);
    }

    // 포인트 관련 예외
    public static BusinessException pointNotFound(Long userId) {
        return new BusinessException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. userId: " + userId);
    }

    public static BusinessException pointInsufficient(Long userId, Integer requested, Integer available) {
        return new BusinessException(ErrorCode.POINT_INSUFFICIENT,
                String.format("사용 가능한 포인트가 부족합니다. userId: %d, 요청: %d, 보유: %d",
                        userId, requested, available));
    }

    public static BusinessException pointInvalidAmount(Integer amount) {
        return new BusinessException(ErrorCode.POINT_INVALID_AMOUNT,
                "포인트 금액이 유효하지 않습니다: " + amount);
    }

    public static BusinessException pointAlreadyEarned(String referenceId, String referenceType) {
        return new BusinessException(ErrorCode.POINT_ALREADY_EARNED,
                String.format("이미 적립된 포인트입니다. referenceId: %s, type: %s",
                        referenceId, referenceType));
    }

    public static BusinessException pointHistoryNotFound(Long historyId) {
        return new BusinessException(ErrorCode.POINT_HISTORY_NOT_FOUND,
                "포인트 내역을 찾을 수 없습니다: " + historyId);
    }

    public static BusinessException pointOperationFailed(String operation, String reason) {
        return new BusinessException(ErrorCode.POINT_OPERATION_FAILED,
                String.format("포인트 %s 처리 중 오류가 발생했습니다: %s", operation, reason));
    }

    public static BusinessException pointUsageFailed(Long userId, Integer amount) {
        return new BusinessException(ErrorCode.POINT_USAGE_FAILED,
                String.format("포인트 사용 처리에 실패했습니다. userId: %d, amount: %d", userId, amount));
    }

    // 범용 메서드
    public static BusinessException of(ErrorCode errorCode) {
        return new BusinessException(errorCode);
    }

    public static BusinessException of(ErrorCode errorCode, String message) {
        return new BusinessException(errorCode, message);
    }
}
