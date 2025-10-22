package com.example.solidconnection.common.interceptor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSourceInterceptor {

    private static final Logger DB_PERF = LoggerFactory.getLogger("DB_PERF");
    private final MeterRegistry meterRegistry;

    @Bean
    @Primary
    public DataSource proxyDataSource(DataSourceProperties props) {
        DataSource dataSource = props.initializeDataSourceBuilder().build();

        QueryExecutionListener listener = new QueryExecutionListener() {
            private static final long SLOW_MS = 300;

            @Override
            public void beforeQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {}

            @Override
            public void afterQuery(ExecutionInfo exec, List<QueryInfo> queries) {
                long elapsedMs = exec.getElapsedTime();
                String sql = queries.isEmpty() ? "" : queries.get(0).getQuery();
                String type = guessType(sql);
                String norm = normalize(sql);

                RequestContext rc = RequestContextHolder.getContext();
                String httpMethod = (rc != null && rc.getHttpMethod() != null) ? rc.getHttpMethod() : "-";
                String httpPath   = (rc != null && rc.getBestMatchPath() != null) ? rc.getBestMatchPath() : "-";

                Timer.builder("db.query")
                        .tag("sql_type", type)
                        .tag("http_method", httpMethod)
                        .tag("http_path", httpPath)
                        .register(meterRegistry)
                        .record(elapsedMs, TimeUnit.MILLISECONDS);

                if (elapsedMs > SLOW_MS) {
                    DB_PERF.warn(
                            "type=DB_SLOW http_method={}, http_path={} type={} elapsed_ms={} success={} sql_norm={}",
                            httpMethod, httpPath, type, elapsedMs, exec.isSuccess(), shorten(norm, 400)
                    );
                } else {
                    DB_PERF.info(
                            "type=DB_SQL http_method={} http_path={} type={} elapsed_ms={} success={}  sql_norm={}",
                            httpMethod, httpPath, type, elapsedMs, exec.isSuccess(), shorten(norm, 400)
                    );
                }
            }
        };
        return ProxyDataSourceBuilder
                .create(dataSource)
                .listener(listener)
                .name("main")
                .build();
    }

    private String normalize(String sql) {
        if (sql == null) return "";
        // 문자열/숫자/공백/IN(...) 단순 정규화
        String s = sql
                .replaceAll("'[^']*'", "?")
                .replaceAll("\\b\\d+\\b", "?")
                .replaceAll("\\((\\s*\\?,\\s*)+\\?\\)", "(?)") // IN (?, ?, ?) -> (?)
                .replaceAll("\\s+", " ")
                .trim();
        return s;
    }

    private String guessType(String sql) {
        if (sql == null) return "OTHER";
        String s = sql.trim().toUpperCase();
        if (s.startsWith("SELECT")) return "SELECT";
        if (s.startsWith("INSERT")) return "INSERT";
        if (s.startsWith("UPDATE")) return "UPDATE";
        if (s.startsWith("DELETE")) return "DELETE";
        return "UNKNOWN";
    }

    private String shorten(String s, int max){ return (s != null && s.length() > max) ? s.substring(0, max) + "..." : s; }

}
