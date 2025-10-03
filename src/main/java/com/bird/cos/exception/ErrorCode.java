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

    ORDER_NOT_FOUND(HttpStatus.BAD_REQUEST, "O001", "주문을 찾을 수 없습니다."),
    ORDER_ACCESS_DENIED(HttpStatus.BAD_REQUEST, "O002", "주문에 접근할 수 없습니다."),

    CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "C001", "코드가 존재하지 않습니다."),
    COUPON_NOT_FOUND(HttpStatus.BAD_REQUEST, "CP001", "쿠폰이 존재하지 않습니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "CP002", "만료되었거나 비활성화된 쿠폰입니다."),
    COUPON_USED(HttpStatus.BAD_REQUEST, "CP003", "이미 사용된 쿠폰입니다."),

    // 포인트 관련
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "PT001", "포인트 정보를 찾을 수 없습니다."),
    POINT_INSUFFICIENT(HttpStatus.BAD_REQUEST, "PT002", "사용 가능한 포인트가 부족합니다."),
    POINT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "PT003", "포인트 금액이 유효하지 않습니다."),
    POINT_ALREADY_EARNED(HttpStatus.CONFLICT, "PT004", "이미 적립된 포인트입니다."),
    POINT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PT005", "포인트 내역을 찾을 수 없습니다."),
    POINT_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PT006", "포인트 처리 중 오류가 발생했습니다."),
    POINT_USAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PT007", "포인트 사용 처리에 실패했습니다."),

    // post 관련
    POST_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PO001", "게시글 저장에 실패했습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "PO002", "게시글을 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
