package com.example.solidconnection.common.interceptor;

import lombok.Getter;

@Getter
public class RequestContext {
    private final String httpMethod;
    private final String bestMatchPath;

    public RequestContext(String httpMethod, String bestMatchPath) {
        this.httpMethod = httpMethod;
        this.bestMatchPath = bestMatchPath;
    }
}
