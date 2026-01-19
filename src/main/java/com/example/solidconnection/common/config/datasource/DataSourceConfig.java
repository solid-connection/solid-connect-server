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
public class DataSourceConfig {

    private final QueryMetricsListener queryMetricsListener;

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
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setPoolName("FlywayPool");

        dataSource.setMinimumIdle(0);
        dataSource.setMaximumPoolSize(2);
        dataSource.setConnectionTimeout(10000);
        dataSource.setIdleTimeout(60000);       // 1분으로 단축
        dataSource.setMaxLifetime(300000);      // 최대 5분

        return dataSource;
    }
}
