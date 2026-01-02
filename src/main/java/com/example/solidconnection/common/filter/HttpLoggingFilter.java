package com.example.solidconnection.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> EXCLUDE_PATTERNS = List.of(
            "/actuator/**"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1) traceId 부여
        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        boolean excluded = isExcluded(request);

        // 2) 로깅 제외 대상이면 그냥 통과 (traceId는 유지: 추후 하위 레이어 로그에도 붙음)
        if (excluded) {
            try {
                filterChain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
            return;
        }

        printRequestUri(request);

        try {
            filterChain.doFilter(request, response);

            Boolean alreadyExceptionLogging = (Boolean) request.getAttribute("exceptionHandlerLogged");
            if (alreadyExceptionLogging == null || !alreadyExceptionLogging) {
                printResponse(request, response);
            }

        } finally {
            MDC.clear();
        }
    }

    private boolean isExcluded(HttpServletRequest req) {
        String path = req.getRequestURI();
        for (String p : EXCLUDE_PATTERNS) {
            if (PATH_MATCHER.match(p, path)) {
                return true;
            }
        }
        return false;
    }

    private String generateTraceId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private void printResponse(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Long userId = (Long) request.getAttribute("userId");
        String uri = buildDecodedRequestUri(request);
        HttpStatus status = HttpStatus.valueOf(response.getStatus());

        log.info("[RESPONSE] {} userId = {}, ({})", uri, userId, status);
    }

    private void printRequestUri(HttpServletRequest request) {
        String methodType = request.getMethod();
        String uri = buildDecodedRequestUri(request);
        log.info("[REQUEST] {} {}", methodType, uri);
    }

    private String decodeQuery(String rawQuery) {
        if (rawQuery == null) {
            return null;
        }
        try {
            return URLDecoder.decode(rawQuery, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return rawQuery;
        }
    }

    private String buildDecodedRequestUri(HttpServletRequest request) {
        String path = request.getRequestURI();
        String query = decodeQuery(request.getQueryString());
        return (query == null || query.isBlank()) ? path : path + "?" + query;
    }
}
