package com.example.solidconnection.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@DisplayName("HttpLoggingFilter 테스트")
class HttpLoggingFilterTest {

    private HttpLoggingFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        filter = new HttpLoggingFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        logger = (Logger) LoggerFactory.getLogger(HttpLoggingFilter.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Nested
    class TraceId_생성 {

        @Test
        void 요청마다_traceId를_생성한다() throws ServletException, IOException {
            // given
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("GET");
            when(response.getStatus()).thenReturn(200);

            AtomicReference<String> capturedTraceId = new AtomicReference<>();

            doAnswer(invocation ->{
                capturedTraceId.set(MDC.get("traceId"));
                return null;
            }).when(filterChain).doFilter(request, response);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            String traceId = capturedTraceId.get();
            assertAll(
                    () -> assertThat(traceId).isNotNull(),
                    () -> assertThat(traceId).hasSize(16),
                    () -> assertThat(traceId).matches("[a-f0-9]{16}")
            );
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    class 로깅_제외_패턴 {

        @Test
        void actuator_경로는_로깅에서_제외된다() throws ServletException, IOException {
            // given
            when(request.getRequestURI()).thenReturn("/actuator/health");
            when(request.getMethod()).thenReturn("GET");

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertAll(
                    () -> assertThat(listAppender.list).noneMatch(event -> event.getFormattedMessage().contains("[REQUEST]")),
                    () -> assertThat(listAppender.list).noneMatch(event -> event.getFormattedMessage().contains("[RESPONSE]"))
            );
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void 일반_경로는_로깅된다() throws ServletException, IOException {
            // given
            when(request.getRequestURI()).thenReturn("/api/users");
            when(request.getMethod()).thenReturn("GET");
            when(response.getStatus()).thenReturn(200);
            String expectedRequestLog = "[REQUEST] GET /api/users";
            String expectedResponseLog = "[RESPONSE] /api/users userId = null, (200 OK)";


            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertAll(
                    () -> assertThat(listAppender.list).anyMatch(event -> event.getFormattedMessage().contains(expectedRequestLog)),
                    () -> assertThat(listAppender.list).anyMatch(event -> event.getFormattedMessage().contains(expectedResponseLog))
            );
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    class 민감한_쿼리_파라미터_마스킹 {

        @Test
        void token_파라미터는_마스킹된다() throws ServletException, IOException {
            // given
            when(request.getRequestURI()).thenReturn("/api/auth");
            when(request.getQueryString()).thenReturn("token=secret123&userId=1");
            when(request.getMethod()).thenReturn("GET");
            when(response.getStatus()).thenReturn(200);
            String expectedRequestLog = "[REQUEST] GET /api/auth?token=****&userId=1";
            String expectedResponseLog = "[RESPONSE] /api/auth?token=****&userId=1 userId = null, (200 OK)";

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertAll(
                    () -> assertThat(listAppender.list).anyMatch(event -> event.getFormattedMessage().contains(expectedRequestLog)),
                    () -> assertThat(listAppender.list).anyMatch(event -> event.getFormattedMessage().contains(expectedResponseLog))
            );
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void 일반_파라미터는_마스킹되지_않는다() throws ServletException, IOException {
            // given
            when(request.getRequestURI()).thenReturn("/api/users");
            when(request.getQueryString()).thenReturn("name=홍길동&age=20");
            when(request.getMethod()).thenReturn("GET");
            when(response.getStatus()).thenReturn(200);
            String expectedRequestLog = "[REQUEST] GET /api/users?name=홍길동&age=20";
            String expectedResponseLog = "[RESPONSE] /api/users?name=홍길동&age=20 userId = null, (200 OK)";

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertAll(
                    () -> assertThat(listAppender.list).anyMatch(event -> event.getFormattedMessage().contains(expectedRequestLog)),
                    () -> assertThat(listAppender.list).anyMatch(event -> event.getFormattedMessage().contains(expectedResponseLog))
            );
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    class 쿼리_파라미터_디코딩 {

        @Test
        void URL_인코딩된_파라미터를_디코딩한다() throws ServletException, IOException {
            // given
            when(request.getRequestURI()).thenReturn("/api/search");
            when(request.getQueryString()).thenReturn("keyword=%ED%99%8D%EA%B8%B8%EB%8F%99");
            when(request.getMethod()).thenReturn("GET");
            when(response.getStatus()).thenReturn(200);
            String expectedParameter = "홍길동";
            String expectedRequestLog = "[REQUEST] GET /api/search?keyword=" + expectedParameter;
            String expectedResponseLog = "[RESPONSE] /api/search?keyword=" + expectedParameter + " userId = null, (200 OK)";

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertAll(
                    () -> assertThat(listAppender.list).anyMatch(event -> event.getFormattedMessage().contains(expectedRequestLog)),
                    () -> assertThat(listAppender.list).anyMatch(event -> event.getFormattedMessage().contains(expectedResponseLog))
            );
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void 디코딩_실패_시_원본_쿼리를_사용한다() throws ServletException, IOException {
            // given
            when(request.getRequestURI()).thenReturn("/api/search");
            when(request.getQueryString()).thenReturn("invalid=%");
            when(request.getMethod()).thenReturn("GET");
            when(response.getStatus()).thenReturn(200);
            String expectedRequestLog = "[REQUEST] GET /api/search?invalid=%";
            String expectedResponseLog = "[RESPONSE] /api/search?invalid=% userId = null, (200 OK)";

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertAll(
                    () -> assertThat(listAppender.list).anyMatch(event -> event.getFormattedMessage().contains(expectedRequestLog)),
                    () -> assertThat(listAppender.list).anyMatch(event -> event.getFormattedMessage().contains(expectedResponseLog))
            );
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    class MDC_정리 {

        @Test
        void 요청_완료_후_MDC가_정리된다() throws ServletException, IOException {
            // given
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("GET");
            when(response.getStatus()).thenReturn(200);

            // when
            filter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(MDC.get("traceId")).isNull();
        }
    }
}
