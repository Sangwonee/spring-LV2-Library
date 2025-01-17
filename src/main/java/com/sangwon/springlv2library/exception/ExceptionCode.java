package com.sangwon.springlv2library.exception;

import lombok.Getter;

public enum ExceptionCode {
    BOOK_NOT_FOUND(404, "책을 찾을 수 없습니다."),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    USER_HAS_UNRETURNED_BOOKS(400, "반납하지 않은 책이 있습니다."),
    BOOK_NOT_AVAILABLE(400, "이미 대출된 책입니다."),
    RENT_NOT_FOUND(404, "대출 기록을 찾을 수 없습니다."),
    USER_HAS_PENALTY(403, "연체 패널티로 인해 대출이 불가능 합니다.");




    @Getter
    private int status;

    @Getter
    private String message;

    ExceptionCode(int code, String message) {
        this.status = code;
        this.message = message;
    }
}
