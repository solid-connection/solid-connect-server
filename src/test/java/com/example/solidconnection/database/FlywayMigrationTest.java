package com.example.solidconnection.database;

import com.example.solidconnection.support.RedisTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest
@ContextConfiguration(initializers = {RedisTestContainer.class, FlywayMigrationTest.FlywayMySQLInitializer.class})
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.baseline-on-migrate=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class FlywayMigrationTest {

    private static final MySQLContainer<?> CONTAINER = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("flyway_test")
            .withUsername("flyway_user")
            .withPassword("flyway_password");

    static {
        CONTAINER.start();
    }

    static class FlywayMySQLInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + CONTAINER.getJdbcUrl(),
                    "spring.datasource.username=" + CONTAINER.getUsername(),
                    "spring.datasource.password=" + CONTAINER.getPassword()
            ).applyTo(applicationContext.getEnvironment());
        }
    }

    @Test
    void flyway_스크립트가_정상적으로_수행되는지_확인한다() {
        // Spring Boot 컨텍스트가 정상적으로 시작되면
        // Flyway 마이그레이션과 ddl-auto=validate 검증이 성공한 것
    }
}
