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

    // 범용 메서드
    public static BusinessException of(ErrorCode errorCode) {
        return new BusinessException(errorCode);
    }

    public static BusinessException of(ErrorCode errorCode, String message) {
        return new BusinessException(errorCode, message);
    }
}
