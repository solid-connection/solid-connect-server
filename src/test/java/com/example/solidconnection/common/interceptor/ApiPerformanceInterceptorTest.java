package com.example.solidconnection.common.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

@DisplayName("ApiPerformanceInterceptor 테스트")
class ApiPerformanceInterceptorTest {

    private ApiPerformanceInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object handler;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        interceptor = new ApiPerformanceInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = new Object();

        logger = (Logger) LoggerFactory.getLogger("API_PERF");
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Nested
    class PreHandle_메서드 {

        @Test
        void 시작_시간을_request에_저장한다() throws Exception {
            // given
            when(request.getRequestURI()).thenReturn("/api/test");
            long beforeTime = System.currentTimeMillis();

            // when
            interceptor.preHandle(request, response, handler);

            // then
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

            verify(request, times(2)).setAttribute(keyCaptor.capture(), valueCaptor.capture());

            List<String> capturedKeys = keyCaptor.getAllValues();
            List<Object> capturedValues = valueCaptor.getAllValues();

            assertThat(capturedKeys).contains("startTime");
            Long startTime = (Long) capturedValues.get(capturedKeys.indexOf("startTime"));
            assertThat(startTime)
                    .isGreaterThanOrEqualTo(beforeTime);

            assertThat(capturedKeys).contains("requestUri");
            String uri = (String) capturedValues.get(capturedKeys.indexOf("requestUri"));
            assertThat(uri).isEqualTo("/api/test");
        }

        @Test
        void preHandle_항상_true를_반환한다() throws Exception {
            // given
            when(request.getRequestURI()).thenReturn("/api/test");

            // when
            boolean result = interceptor.preHandle(request, response, handler);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    class AfterCompletion_메서드 {

        @Test
        void 응답_시간을_계산하고_로그를_남긴다() throws Exception {
            // given
            long startTime = System.currentTimeMillis();
            when(request.getAttribute("startTime")).thenReturn(startTime);
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("GET");
            when(response.getStatus()).thenReturn(200);
            String expectedApiPerfLog = "type=API_Performance";

            // when
            interceptor.afterCompletion(request, response, handler, null);

            // then
            ILoggingEvent logEvent = listAppender.list.stream()
                    .filter(event -> event.getFormattedMessage().contains(expectedApiPerfLog))
                    .findFirst()
                    .orElseThrow();
            assertAll(
                    () -> assertThat(logEvent.getLevel().toString()).isEqualTo("INFO"),
                    () -> assertThat(logEvent.getFormattedMessage()).contains("uri=/api/test"),
                    () -> assertThat(logEvent.getFormattedMessage()).contains("method_type=GET"),
                    () -> assertThat(logEvent.getFormattedMessage()).contains("status=200")
            );
        }

        @Test
        void 응답_시간이_3초를_초과하면_WARN_로그를_남긴다() throws Exception {
            // given
            long startTime = System.currentTimeMillis() - 4000; // 4초 전
            when(request.getAttribute("startTime")).thenReturn(startTime);
            when(request.getRequestURI()).thenReturn("/api/slow");
            when(request.getMethod()).thenReturn("GET");
            when(response.getStatus()).thenReturn(200);
            String expectedApiPerfLog = "type=API_Performance";

            // when
            interceptor.afterCompletion(request, response, handler, null);

            // then
            ILoggingEvent logEvent = listAppender.list.stream()
                    .filter(event -> event.getFormattedMessage().contains(expectedApiPerfLog))
                    .findFirst()
                    .orElseThrow();
            assertAll(
                    () -> assertThat(logEvent.getLevel().toString()).isEqualTo("WARN"),
                    () -> assertThat(logEvent.getFormattedMessage()).contains("uri=/api/slow"),
                    () -> assertThat(logEvent.getFormattedMessage()).contains("method_type=GET"),
                    () -> assertThat(logEvent.getFormattedMessage()).contains("status=200")
            );
        }

        @Test
        void startTime이_없으면_로그를_남기지_않는다() throws Exception {
            // given
            when(request.getAttribute("startTime")).thenReturn(null);
            String noExpectedApiPerfLog = "type=API_Performance";

            // when
            interceptor.afterCompletion(request, response, handler, null);

            // then
            assertThat(listAppender.list).noneMatch(event -> event.getFormattedMessage().contains(noExpectedApiPerfLog));
        }
    }

    @Nested
    class 예외_발생_시 {

        @Test
        void 예외가_발생해도_로그를_정상_기록한다() throws Exception {
            // given
            long startTime = System.currentTimeMillis();
            when(request.getAttribute("startTime")).thenReturn(startTime);
            when(request.getRequestURI()).thenReturn("/api/error");
            when(request.getMethod()).thenReturn("GET");
            when(response.getStatus()).thenReturn(500);

            Exception ex = new RuntimeException("Test exception");

            String expectedApiPerfLog = "type=API_Performance";

            // when
            interceptor.afterCompletion(request, response, handler, ex);

            // then
            ILoggingEvent logEvent = listAppender.list.stream()
                    .filter(event -> event.getFormattedMessage().contains(expectedApiPerfLog))
                    .findFirst()
                    .orElseThrow();
            assertAll(
                    () -> assertThat(logEvent.getLevel().toString()).isEqualTo("INFO"),
                    () -> assertThat(logEvent.getFormattedMessage()).contains("uri=/api/error"),
                    () -> assertThat(logEvent.getFormattedMessage()).contains("method_type=GET"),
                    () -> assertThat(logEvent.getFormattedMessage()).contains("status=500")
            );
        }
    }
}
