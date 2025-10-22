package com.example.solidconnection.common.interceptor;

public class RequestContextHolder {
    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    public static void initContext(RequestContext requestContext) {
        CONTEXT.remove();
        CONTEXT.set(requestContext);
    }

    public static RequestContext getContext() {
        return CONTEXT.get();
    }

    public static void clear(){
        CONTEXT.remove();
    }
}
