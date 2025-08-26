package com.example.solidconnection.common.response;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;

public record ErrorResponse(String message) {

    public ErrorResponse(CustomException e) {
        this(e.getMessage());
    }

    public ErrorResponse(ErrorCode e) {
        this(e.getMessage());
    }

    public ErrorResponse(ErrorCode e, String detail) {
        this(e.getMessage() + " : " + detail);
    }
}
