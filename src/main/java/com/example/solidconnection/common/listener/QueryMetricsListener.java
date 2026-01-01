package com.example.solidconnection.common.listener;

import com.example.solidconnection.common.interceptor.RequestContext;
import com.example.solidconnection.common.interceptor.RequestContextHolder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class QueryMetricsListener implements QueryExecutionListener {

    private final MeterRegistry meterRegistry;

    @Override
    public void beforeQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {

    }

    @Override
    public void afterQuery(ExecutionInfo exec, List<QueryInfo> queries) {
        long elapsedMs = exec.getElapsedTime();
        String sql = queries.isEmpty() ? "" : queries.get(0).getQuery();
        String type = guessType(sql);

        RequestContext rc = RequestContextHolder.getContext();
        String httpMethod = (rc != null && rc.getHttpMethod() != null) ? rc.getHttpMethod() : "-";
        String httpPath = (rc != null && rc.getBestMatchPath() != null) ? rc.getBestMatchPath() : "-";

        Timer.builder("db.query")
                .tag("sql_type", type)
                .tag("http_method", httpMethod)
                .tag("http_path", httpPath)
                .register(meterRegistry)
                .record(elapsedMs, TimeUnit.MILLISECONDS);
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
}
