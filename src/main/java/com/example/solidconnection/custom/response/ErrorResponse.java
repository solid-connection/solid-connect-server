package com.example.solidconnection.custom.response;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;

public record ErrorResponse(
        Boolean success,
        ErrorDetail error) implements CustomResponse {

    private record ErrorDetail(
            int code,
            String message) {
    }

    public ErrorResponse(CustomException e) {
        this(false, new ErrorDetail(e.getCode(), e.getMessage()));
    }

    public ErrorResponse(ErrorCode e, String detail){
        this(false, new ErrorDetail(e.getCode(), e.getMessage() + " : " + detail));
    }
}
