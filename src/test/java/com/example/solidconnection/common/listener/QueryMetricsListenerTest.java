package com.example.solidconnection.common.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.solidconnection.common.interceptor.RequestContext;
import com.example.solidconnection.common.interceptor.RequestContextHolder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("QueryMetricsListener 테스트")
class QueryMetricsListenerTest {

    private QueryMetricsListener listener;
    private MeterRegistry meterRegistry;
    private ExecutionInfo executionInfo;

    @BeforeEach
    void setUp() {
        meterRegistry = mock(MeterRegistry.class);
        listener = new QueryMetricsListener(meterRegistry);
        executionInfo = mock(ExecutionInfo.class);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Nested
    class 쿼리_메트릭_수집 {

        @Test
        void SELECT_쿼리의_실행_시간을_기록한다() {
            // given
            String sql = "SELECT * FROM users WHERE id = ?";
            QueryInfo queryInfo = new QueryInfo();
            queryInfo.setQuery(sql);

            when(executionInfo.getElapsedTime()).thenReturn(100L);

            Timer timer = mock(Timer.class);
            when(meterRegistry.timer(
                    eq("db.query"),
                    eq("sql_type"), any(String.class),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            )).thenReturn(timer);

            // when
            listener.afterQuery(executionInfo, List.of(queryInfo));

            // then
            verify(meterRegistry).timer(
                    eq("db.query"),
                    eq("sql_type"), eq("SELECT"),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            );
            verify(timer).record(100L, TimeUnit.MILLISECONDS);
        }

        @Test
        void INSERT_쿼리의_실행_시간을_기록한다() {
            // given
            String sql = "INSERT INTO users (name) VALUES (?)";
            QueryInfo queryInfo = new QueryInfo();
            queryInfo.setQuery(sql);

            when(executionInfo.getElapsedTime()).thenReturn(100L);

            Timer timer = mock(Timer.class);
            when(meterRegistry.timer(
                    eq("db.query"),
                    eq("sql_type"), any(String.class),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            )).thenReturn(timer);

            // when
            listener.afterQuery(executionInfo, List.of(queryInfo));

            // then
            verify(meterRegistry).timer(
                    eq("db.query"),
                    eq("sql_type"), eq("INSERT"),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            );
            verify(timer).record(100L, TimeUnit.MILLISECONDS);
        }

        @Test
        void UPDATE_쿼리의_실행_시간을_기록한다() {
            // given
            String sql = "UPDATE users SET name = ? WHERE id = ?";
            QueryInfo queryInfo = new QueryInfo();
            queryInfo.setQuery(sql);

            when(executionInfo.getElapsedTime()).thenReturn(100L);

            Timer timer = mock(Timer.class);
            when(meterRegistry.timer(
                    eq("db.query"),
                    eq("sql_type"), eq("UPDATE"),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            )).thenReturn(timer);

            // when
            listener.afterQuery(executionInfo, List.of(queryInfo));

            // then
            verify(meterRegistry).timer(
                    eq("db.query"),
                    eq("sql_type"), eq("UPDATE"),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            );
            verify(timer).record(100L, TimeUnit.MILLISECONDS);
        }

        @Test
        void DELETE_쿼리의_실행_시간을_기록한다() {
            // given
            String sql = "DELETE FROM users WHERE id = ?";
            QueryInfo queryInfo = new QueryInfo();
            queryInfo.setQuery(sql);

            when(executionInfo.getElapsedTime()).thenReturn(100L);

            Timer timer = mock(Timer.class);
            when(meterRegistry.timer(
                    eq("db.query"),
                    eq("sql_type"), eq("DELETE"),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            )).thenReturn(timer);

            // when
            listener.afterQuery(executionInfo, List.of(queryInfo));

            // then
            verify(meterRegistry).timer(
                    eq("db.query"),
                    eq("sql_type"), eq("DELETE"),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            );
            verify(timer).record(100L, TimeUnit.MILLISECONDS);
        }

        @Test
        void 알수없는_쿼리는_UNKNOWN으로_기록한다() {
            // given
            String sql = "SHOW TABLES";
            QueryInfo queryInfo = new QueryInfo();
            queryInfo.setQuery(sql);

            when(executionInfo.getElapsedTime()).thenReturn(100L);

            Timer timer = mock(Timer.class);
            when(meterRegistry.timer(
                    eq("db.query"),
                    eq("sql_type"), any(String.class),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            )).thenReturn(timer);

            // when
            listener.afterQuery(executionInfo, List.of(queryInfo));

            // then
            verify(meterRegistry).timer(
                    eq("db.query"),
                    eq("sql_type"), eq("UNKNOWN"),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            );
            verify(timer).record(100L, TimeUnit.MILLISECONDS);
        }

        @Test
        void null_쿼리는_OTHER로_기록한다() {
            // given
            QueryInfo queryInfo = new QueryInfo();
            when(executionInfo.getElapsedTime()).thenReturn(100L);

            Timer timer = mock(Timer.class);
            when(meterRegistry.timer(
                    eq("db.query"),
                    eq("sql_type"), any(String.class),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            )).thenReturn(timer);

            // when
            listener.afterQuery(executionInfo, List.of(queryInfo));

            // then
            verify(meterRegistry).timer(
                    eq("db.query"),
                    eq("sql_type"), eq("OTHER"),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            );
            verify(timer).record(100L, TimeUnit.MILLISECONDS);
        }
    }

    @Nested
    class RequestContext_연동 {

        @Test
        void RequestContext가_있으면_HTTP_정보를_포함한다() {
            // given
            RequestContext context = new RequestContext("GET", "/api/users");
            RequestContextHolder.initContext(context);

            String sql = "SELECT * FROM users";
            QueryInfo queryInfo = new QueryInfo();
            queryInfo.setQuery(sql);

            when(executionInfo.getElapsedTime()).thenReturn(100L);

            Timer timer = mock(Timer.class);
            when(meterRegistry.timer(
                    eq("db.query"),
                    eq("sql_type"), any(String.class),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            )).thenReturn(timer);

            // when
            listener.afterQuery(executionInfo, List.of(queryInfo));

            // then
            verify(meterRegistry).timer(
                    eq("db.query"),
                    eq("sql_type"), eq("SELECT"),
                    eq("http_method"), eq("GET"),
                    eq("http_path"), eq("/api/users")
            );
            verify(timer).record(100L, TimeUnit.MILLISECONDS);
        }

        @Test
        void RequestContext가_없으면_기본값을_사용한다() {
            // given
            String sql = "SELECT * FROM users";
            QueryInfo queryInfo = new QueryInfo();
            queryInfo.setQuery(sql);

            when(executionInfo.getElapsedTime()).thenReturn(100L);

            Timer timer = mock(Timer.class);
            when(meterRegistry.timer(
                    eq("db.query"),
                    eq("sql_type"), any(String.class),
                    eq("http_method"), any(String.class),
                    eq("http_path"), any(String.class)
            )).thenReturn(timer);

            // when
            listener.afterQuery(executionInfo, List.of(queryInfo));

            // then
            verify(meterRegistry).timer(
                    eq("db.query"),
                    eq("sql_type"), eq("SELECT"),
                    eq("http_method"), eq("-"),
                    eq("http_path"), eq("-")
            );
            verify(timer).record(100L, TimeUnit.MILLISECONDS);
        }
    }
}
