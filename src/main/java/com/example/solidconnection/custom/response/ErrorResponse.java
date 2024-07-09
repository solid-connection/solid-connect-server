package com.example.solidconnection.custom.response;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;

public record ErrorResponse(String message) { // todo: 이 부분 바뀌었다고 말씀드리기

    public ErrorResponse(CustomException e) {
        this(e.getMessage());
    }

    public ErrorResponse(ErrorCode e, String detail) {
        this(e.getMessage() + " : " + detail);
    }
}
