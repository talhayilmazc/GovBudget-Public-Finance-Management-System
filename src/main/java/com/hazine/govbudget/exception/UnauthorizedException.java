package com.hazine.govbudget.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }
}