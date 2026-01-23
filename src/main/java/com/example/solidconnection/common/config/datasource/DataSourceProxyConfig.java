package com.example.solidconnection.common.config.datasource;

import com.example.solidconnection.common.listener.QueryMetricsListener;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@RequiredArgsConstructor
@Configuration
public class DataSourceProxyConfig {

    private final QueryMetricsListener queryMetricsListener;

    // Driver
    public static final String FLYWAY_MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";

    // Pool Name
    public static final String FLYWAY_POOL_NAME = "FlywayPool";

    // Connection Pool Settings
    public static final int FLYWAY_MINIMUM_IDLE = 0;            // 유휴 커넥션을 0으로 설정하면 사용하지 않을 때 커넥션을 즉시 반납
    public static final int FLYWAY_MAXIMUM_POOL_SIZE = 2;
    public static final long FLYWAY_CONNECTION_TIMEOUT = 10000L;
    public static final long FLYWAY_IDLE_TIMEOUT = 60000L;      // 1분
    public static final long FLYWAY_MAX_LIFETIME = 300000L;     // 5분

    @Bean
    @Primary
    public DataSource proxyDataSource(DataSourceProperties props) {
        DataSource dataSource = props.initializeDataSourceBuilder().build();

        return ProxyDataSourceBuilder
                .create(dataSource)
                .listener(queryMetricsListener)
                .name("main")
                .build();
    }

    // Flyway 전용 DataSource (Proxy 미적용)
    @Bean
    @FlywayDataSource
    public DataSource flywayDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.flyway.user:${spring.datasource.username}}") String username,
            @Value("${spring.flyway.password:${spring.datasource.password}}") String password
    ) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(FLYWAY_MYSQL_DRIVER);
        dataSource.setPoolName(FLYWAY_POOL_NAME);

        dataSource.setMinimumIdle(FLYWAY_MINIMUM_IDLE);
        dataSource.setMaximumPoolSize(FLYWAY_MAXIMUM_POOL_SIZE);
        dataSource.setConnectionTimeout(FLYWAY_CONNECTION_TIMEOUT);
        dataSource.setIdleTimeout(FLYWAY_IDLE_TIMEOUT);       // 1분으로 단축
        dataSource.setMaxLifetime(FLYWAY_MAX_LIFETIME);      // 최대 5분

        return dataSource;
    }
}
