package com.example.solidconnection.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
@Component
public class HttpLoggingFilter extends OncePerRequestFilter {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    // 필요 시 자유롭게 추가/수정
    private static final List<String> EXCLUDE_PATTERNS = List.of(
            "/actuator/**"      // 전체 액추에이터
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1) traceId 부여 (MDC)
        final String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        // 2) 로깅 제외 대상이면 그냥 통과 (traceId는 유지: 추후 하위 레이어 로그에도 붙음)
        if (isExcluded(request)) {
            try {
                filterChain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } catch (Throwable t) {
            // 3) 예외 중복 로깅 방지: 글로벌 예외 핸들러가 이미 찍었는지 확인
            Boolean alreadyLogged = (Boolean) request.getAttribute("errorLoggedByGlobal");
            if (alreadyLogged == null || !alreadyLogged) {
                // 최소 정보만 한 줄: 중복·과로 깔끔히 방지
                log.error("[UNHANDLED-ERROR] traceId={} method={} uri={}",
                          traceId, request.getMethod(), request.getRequestURI(), t);
                // 다른 레이어가 중복으로 찍지 않도록 표식 남김(선택)
                request.setAttribute("errorLoggedByFilter", true);
            }
            // 예외는 반드시 재던져야 스프링 예외 처리/트랜잭션이 정상 동작
            throw t;
        } finally {
            MDC.clear();
        }
    }

    private boolean isExcluded(HttpServletRequest req) {
        String path = req.getRequestURI();
        for (String p : EXCLUDE_PATTERNS) {
            if (PATH_MATCHER.match(p, path)) return true;
        }
        return false;
    }

    private String generateTraceId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
