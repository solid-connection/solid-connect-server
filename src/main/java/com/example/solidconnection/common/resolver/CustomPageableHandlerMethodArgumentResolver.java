package com.example.solidconnection.common.resolver;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_PAGE_PARAMETER;

import com.example.solidconnection.common.exception.CustomException;
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
    private static final int MIN_ONE_INDEXED_PAGE = 1;

    public CustomPageableHandlerMethodArgumentResolver() {
        setMaxPageSize(MAX_SIZE);
        setOneIndexedParameters(true);
        setFallbackPageable(PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE));
    }

    @Override
    public Pageable resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        validatePageParameter(methodParameter, webRequest);
        return super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
    }

    private void validatePageParameter(MethodParameter methodParameter, NativeWebRequest webRequest) {
        String parameterName = getParameterNameToUse(getPageParameterName(), methodParameter);
        String pageParameter = webRequest.getParameter(parameterName);
        if (pageParameter == null || pageParameter.isBlank()) {
            return;
        }
        try {
            if (Integer.parseInt(pageParameter) < MIN_ONE_INDEXED_PAGE) {
                throw new CustomException(INVALID_PAGE_PARAMETER);
            }
        } catch (NumberFormatException e) {
            // 숫자로 파싱할 수 없는 값은 기존과 동일하게 상위 리졸버가 기본값으로 대체한다.
        }
    }
}
