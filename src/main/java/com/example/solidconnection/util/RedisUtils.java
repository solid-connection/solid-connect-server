package com.example.solidconnection.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RedisUtils {

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisUtils(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public List<String> getKeysOrderByExpiration(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        return keys.stream()
                .sorted(Comparator.comparingLong(this::getExpirationTime))
                .collect(Collectors.toList());
    }

    public Long getExpirationTime(String key) {
        return redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
    }
}
