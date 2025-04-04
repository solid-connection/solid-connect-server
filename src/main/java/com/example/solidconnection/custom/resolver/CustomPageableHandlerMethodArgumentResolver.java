package com.example.solidconnection.custom.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

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

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter,
                                    ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest,
                                    WebDataBinderFactory binderFactory) {
        return super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
    }
}
