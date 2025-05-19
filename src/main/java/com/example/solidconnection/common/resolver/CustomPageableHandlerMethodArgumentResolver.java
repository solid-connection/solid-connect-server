package com.example.solidconnection.common.resolver;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;

@Component
public class CustomPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {

    private static final int DEFAULT_PAGE = 0;
    private static final int MAX_SIZE = 50;
    private static final int DEFAULT_SIZE = 10;

    public CustomPageableHandlerMethodArgumentResolver() {
        setMaxPageSize(MAX_SIZE);
        setOneIndexedParameters(true);
        setFallbackPageable(PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE));
    }
}
