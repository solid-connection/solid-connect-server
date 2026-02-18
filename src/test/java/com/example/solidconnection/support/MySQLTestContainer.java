package com.example.solidconnection.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.mysql.MySQLContainer;

public class MySQLTestContainer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final MySQLContainer CONTAINER = new MySQLContainer("mysql:8.0");

    static {
        CONTAINER.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url=" + CONTAINER.getJdbcUrl(),
                "spring.datasource.username=" + CONTAINER.getUsername(),
                "spring.datasource.password=" + CONTAINER.getPassword()
        ).applyTo(applicationContext.getEnvironment());
    }
}
