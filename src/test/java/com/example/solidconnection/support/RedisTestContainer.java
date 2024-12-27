package com.example.solidconnection.support;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

@TestConfiguration
public class RedisTestContainer {

    @Container
    private static final GenericContainer<?> CONTAINER = new GenericContainer<>("redis:7.0");

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", CONTAINER::getHost);
        registry.add("spring.redis.port", CONTAINER::getFirstMappedPort);
    }

    @PostConstruct
    void startContainer() {
        if (!CONTAINER.isRunning()) {
            CONTAINER.start();
        }
    }
}
