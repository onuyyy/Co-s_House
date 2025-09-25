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
