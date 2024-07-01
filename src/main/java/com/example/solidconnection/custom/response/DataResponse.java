package com.example.solidconnection.custom.response;

public record DataResponse<T>(
        boolean success,
        T data) implements CustomResponse {

    public DataResponse(T data){
        this(true, data);
    }
}
