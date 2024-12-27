package com.example.solidconnection.support;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.sql.DataSource;

@TestConfiguration
public class MySQLTestContainer {

    @Container
    private static final MySQLContainer<?> CONTAINER = new MySQLContainer<>("mysql:8.0");

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(CONTAINER.getJdbcUrl())
                .username(CONTAINER.getUsername())
                .password(CONTAINER.getPassword())
                .driverClassName(CONTAINER.getDriverClassName())
                .build();
    }

    @PostConstruct
    void startContainer() {
        if (!CONTAINER.isRunning()) {
            CONTAINER.start();
        }
    }
}
