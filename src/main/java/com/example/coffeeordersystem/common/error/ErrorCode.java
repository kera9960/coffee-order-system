package com.example.coffeeordersystem.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_MENU_NAME(HttpStatus.BAD_REQUEST, "메뉴 이름이 비어 있거나 올바르지 않음"),
    INVALID_MENU_PRICE(HttpStatus.BAD_REQUEST, "메뉴 가격이 0 이하임"),
    INVALID_MENU_STATUS(HttpStatus.BAD_REQUEST, "지원하지 않는 메뉴 판매 상태임"),
    DUPLICATED_MENU_NAME(HttpStatus.CONFLICT, "같은 이름의 메뉴가 이미 존재함"),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴가 존재하지 않음"),
    INVALID_PAGE_REQUEST(HttpStatus.BAD_REQUEST, "페이지 번호, 크기, 정렬 기준이 올바르지 않음");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
