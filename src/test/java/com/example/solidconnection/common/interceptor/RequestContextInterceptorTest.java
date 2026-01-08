package com.example.solidconnection.common.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RequestContextInterceptor 테스트")
class RequestContextInterceptorTest {

    private RequestContextInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object handler;

    @BeforeEach
    void setUp() {
        interceptor = new RequestContextInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = new Object();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Nested
    class PreHandle_메서드 {

        @Test
        void RequestContext를_초기화_한_후_true를_리턴한다() {
            // given
            when(request.getMethod()).thenReturn("GET");
            when(request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/api/users/{id}");

            // when
            boolean result = interceptor.preHandle(request, response, handler);

            // then
            assertThat(result).isTrue();

            RequestContext context = RequestContextHolder.getContext();
            assertThat(context).isNotNull();
            assertThat(context.getHttpMethod()).isEqualTo("GET");
            assertThat(context.getBestMatchPath()).isEqualTo("/api/users/{id}");
        }

        @Test
        void best_matching_pattern이_null이면_null을_저장한다() {
            // given
            when(request.getMethod()).thenReturn("GET");
            when(request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(null);

            // when
            interceptor.preHandle(request, response, handler);

            // then
            RequestContext context = RequestContextHolder.getContext();
            assertThat(context.getBestMatchPath()).isNull();
        }
    }

    @Nested
    class AfterCompletion_메서드 {

        @Test
        void RequestContext를_정리한다() {
            // given
            when(request.getMethod()).thenReturn("GET");
            when(request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/api/users");

            interceptor.preHandle(request, response, handler);
            assertThat(RequestContextHolder.getContext()).isNotNull();

            // when
            interceptor.afterCompletion(request, response, handler, null);

            // then
            assertThat(RequestContextHolder.getContext()).isNull();
        }

        @Test
        void 예외가_발생해도_RequestContext를_정리한다() {
            // given
            when(request.getMethod()).thenReturn("POST");
            when(request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/api/users");

            interceptor.preHandle(request, response, handler);
            assertThat(RequestContextHolder.getContext()).isNotNull();

            Exception ex = new RuntimeException("Test exception");

            // when
            interceptor.afterCompletion(request, response, handler, ex);

            // then
            assertThat(RequestContextHolder.getContext()).isNull();
        }
    }
}
