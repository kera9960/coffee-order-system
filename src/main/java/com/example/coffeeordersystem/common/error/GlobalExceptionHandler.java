package com.example.coffeeordersystem.common.error;

import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        return toResponse(exception.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        ErrorCode errorCode = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> toErrorCode(fieldError.getField()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(ErrorCode.INVALID_PAGE_REQUEST);
        return toResponse(errorCode);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        if (exception.getMessage() != null && exception.getMessage().contains("PointTransactionType")) {
            return toResponse(ErrorCode.INVALID_POINT_TRANSACTION_TYPE);
        }
        return toResponse(ErrorCode.INVALID_MENU_STATUS);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException() {
        return toResponse(ErrorCode.DUPLICATED_MENU_NAME);
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.from(errorCode));
    }

    private ErrorCode toErrorCode(String field) {
        return switch (field) {
            case "name" -> ErrorCode.INVALID_MENU_NAME;
            case "price" -> ErrorCode.INVALID_MENU_PRICE;
            case "status" -> ErrorCode.INVALID_MENU_STATUS;
            case "amount" -> ErrorCode.INVALID_POINT_AMOUNT;
            default -> null;
        };
    }
}
