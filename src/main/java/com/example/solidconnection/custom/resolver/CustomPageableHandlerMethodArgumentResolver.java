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

    public static final int MAX_SIZE = 50;
    private static final int DEFAULT_SIZE = 10;

    public CustomPageableHandlerMethodArgumentResolver() {
        setOneIndexedParameters(true);
        setFallbackPageable(PageRequest.of(0, 10));
    }

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter,
                                    ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest,
                                    WebDataBinderFactory binderFactory) {
        Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
        if (pageable.getPageSize() > MAX_SIZE) {
            return PageRequest.of(pageable.getPageNumber(), DEFAULT_SIZE, pageable.getSort());
        }
        return pageable;
    }
}
