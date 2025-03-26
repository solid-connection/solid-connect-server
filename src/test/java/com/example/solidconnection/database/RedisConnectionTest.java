package com.example.solidconnection.database;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @DisplayName("레디스 연결 및 작동 테스트")
    @Test
    void connectRedis() {
        String key = "test-key";
        String expectedValue = "test-value";

        redisTemplate.opsForValue().set(key, expectedValue);
        String actualValue = redisTemplate.opsForValue().get(key);

        assertThat(actualValue).isEqualTo(expectedValue);
    }
}
