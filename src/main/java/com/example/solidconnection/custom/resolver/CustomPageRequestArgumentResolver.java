package com.example.solidconnection.custom.resolver;

import com.example.solidconnection.custom.request.CustomPageRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomPageRequestArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String PAGE_PARAMETER = "page";
    private static final String SIZE_PARAMETER = "size";
    private static final String SORT_PARAMETER = "sort";
    private static final String DESC = "desc";
    private static final String COMMA = ",";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return CustomPageRequest.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        int page = extractIntParameter(request, PAGE_PARAMETER, DEFAULT_PAGE);
        int size = extractIntParameter(request, SIZE_PARAMETER, DEFAULT_SIZE);
        Sort sort = extractSortParameter(request);
        return CustomPageRequest.of(page, size, sort);
    }

    private int extractIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String paramValue = request.getParameter(paramName);
        if (paramValue != null && !paramValue.isEmpty()) {
            try {
                return Integer.parseInt(paramValue);
            } catch (NumberFormatException e) {
                // 숫자로 변환할 수 없는 경우 기본값 사용
            }
        }
        return defaultValue;
    }

    private Sort extractSortParameter(HttpServletRequest request) {
        String[] sortParams = request.getParameterValues(SORT_PARAMETER);
        if (sortParams == null || sortParams.length == 0) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (String sortParam : sortParams) {
            addSortOrder(sortParam, orders);
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    private void addSortOrder(String sortParam, List<Sort.Order> orders) {
        String[] parts = sortParam.split(COMMA);
        if (parts.length < 1) {
            return;
        }
        String property = parts[0];
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length >= 2 && DESC.equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        orders.add(new Sort.Order(direction, property));
    }
}
