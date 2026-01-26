package com.example.solidconnection.common.interceptor;

import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String httpMethod = request.getMethod();
        String bestMatchPath = (String) request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);

        RequestContext context = new RequestContext(httpMethod, bestMatchPath);
        RequestContextHolder.initContext(context);

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler, Exception ex
    ) {
        RequestContextHolder.clear();
    }
}
