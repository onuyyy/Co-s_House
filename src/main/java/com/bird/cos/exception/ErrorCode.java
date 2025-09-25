package com.bird.cos.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다"),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "U003", "비밀번호가 일치하지 않습니다"),

    // 인증/인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다"),

    // 비즈니스 로직
    INVALID_OPERATION(HttpStatus.BAD_REQUEST, "B002", "유효하지 않은 작업입니다"),

    // 상품 관련
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다"),
    PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "P002", "재고가 부족합니다"),
    PRODUCT_UNAVAILABLE(HttpStatus.BAD_REQUEST, "P003", "판매 중단된 상품입니다"),
    OPTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "OT001", "선택한 옵션을 찾을 수 없습니다."),
    OPTION_BAD_REQUEST(HttpStatus.BAD_REQUEST, "OT002", "잘못된 상품 옵션입니다."),

    CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "C001", "코드가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
