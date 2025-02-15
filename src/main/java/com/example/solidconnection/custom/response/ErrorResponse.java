package com.example.solidconnection.custom.response;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;

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
