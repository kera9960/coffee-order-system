package com.example.coffeeordersystem.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_MENU_NAME(HttpStatus.BAD_REQUEST, "메뉴 이름이 비어 있거나 올바르지 않음"),
    INVALID_MENU_PRICE(HttpStatus.BAD_REQUEST, "메뉴 가격이 0 이하임"),
    INVALID_MENU_STATUS(HttpStatus.BAD_REQUEST, "지원하지 않는 메뉴 판매 상태임"),
    DUPLICATED_MENU_NAME(HttpStatus.CONFLICT, "같은 이름의 메뉴가 이미 존재함"),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴가 존재하지 않음"),
    MENU_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "판매 중인 메뉴만 주문할 수 있음"),
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "고객이 존재하지 않음"),
    EMPTY_ORDER_ITEMS(HttpStatus.BAD_REQUEST, "주문 항목이 비어 있음"),
    INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "주문 수량이 1보다 작음"),
    DUPLICATED_ORDER_MENU(HttpStatus.BAD_REQUEST, "한 주문 요청에 같은 메뉴가 중복됨"),
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "포인트 잔액이 부족함"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문이 존재하지 않거나 해당 고객의 주문이 아님"),
    INVALID_POINT_AMOUNT(HttpStatus.BAD_REQUEST, "충전 포인트가 0 이하임"),
    INVALID_POINT_TRANSACTION_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 포인트 거래 유형"),
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
