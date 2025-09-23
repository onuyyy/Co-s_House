package com.bird.cos.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * DTO @Valid 검증 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_OPERATION, req.getRequestURI(), errors);
        return ResponseEntity.status(ErrorCode.INVALID_OPERATION.getStatus()).body(body);
    }

    /**
     * 잘못된 요청
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_OPERATION, req.getRequestURI());
        return ResponseEntity.status(ErrorCode.INVALID_OPERATION.getStatus()).body(body);
    }

    /**
     * 인증 실패
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.UNAUTHORIZED, req.getRequestURI());
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getStatus()).body(body);
    }

    /**
     * 그 외 모든 에러
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOthers(Exception ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_OPERATION, req.getRequestURI());
        return ResponseEntity.status(ErrorCode.INVALID_OPERATION.getStatus()).body(body);
    }

    /**
     * 비즈니스 로직 에러
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(ex.getErrorCode(), req.getRequestURI());
        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(body);
    }

    /**
     * 공통 응답 DTO
     */
    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String code,
            String message,
            String path,
            Map<String, String> details
    ) {
        public static ErrorResponse of(ErrorCode errorCode, String path) {
            return new ErrorResponse(
                    Instant.now(),
                    errorCode.getStatus().value(),
                    errorCode.getStatus().getReasonPhrase(),
                    errorCode.getCode(),
                    errorCode.getMessage(),
                    path,
                    null
            );
        }

        public static ErrorResponse of(ErrorCode errorCode, String path, Map<String, String> details) {
            return new ErrorResponse(
                    Instant.now(),
                    errorCode.getStatus().value(),
                    errorCode.getStatus().getReasonPhrase(),
                    errorCode.getCode(),
                    errorCode.getMessage(),
                    path,
                    details
            );
        }
    }
}

