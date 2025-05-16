package com.example.solidconnection.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final int code;
    private final String message;

    public CustomException(ErrorCode errorCode) {
        code = errorCode.getCode();
        message = errorCode.getMessage();
    }

    public CustomException(ErrorCode errorCode, String detail) {
        code = errorCode.getCode();
        message = errorCode.getMessage() + " : " + detail;
    }
}
