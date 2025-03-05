package com.example.solidconnection.custom.resolver;

import com.example.solidconnection.custom.exception.CustomException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_PAGE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_SIZE;

@Component
public class CustomPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {

    public static final int MIN_PAGE = 1;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 50;
    private static final String PAGE_PARAMETER = "page";
    private static final String SIZE_PARAMETER = "size";

    public CustomPageableHandlerMethodArgumentResolver() {
        setMaxPageSize(MAX_SIZE);
        setOneIndexedParameters(true);
        setFallbackPageable(PageRequest.of(0, 10));
    }

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter,
                                    ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest,
                                    WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request != null) {
            validateParameters(request);
        }
        return super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
    }

    private void validateParameters(HttpServletRequest request) {
        int page = extractIntParameter(request, PAGE_PARAMETER, 1);
        int size = extractIntParameter(request, SIZE_PARAMETER, 10);
        if (page < MIN_PAGE) {
            throw new CustomException(INVALID_PAGE);
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new CustomException(INVALID_SIZE);
        }
    }

    private int extractIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String paramValue = request.getParameter(paramName);
        if (StringUtils.isBlank(paramValue)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(paramValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
