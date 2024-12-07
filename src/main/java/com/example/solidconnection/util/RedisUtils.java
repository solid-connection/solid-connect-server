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

import static com.example.solidconnection.type.RedisConstants.CREATE_LOCK_PREFIX;
import static com.example.solidconnection.type.RedisConstants.REFRESH_LOCK_PREFIX;
import static com.example.solidconnection.type.RedisConstants.VALIDATE_VIEW_COUNT_KEY_PREFIX;
import static com.example.solidconnection.type.RedisConstants.VIEW_COUNT_KEY_PREFIX;

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

    public String getPostViewCountRedisKey(Long postId) {
        return VIEW_COUNT_KEY_PREFIX.getValue() + postId;
    }

    public String getValidatePostViewCountRedisKey(String email, Long postId) {
        return VALIDATE_VIEW_COUNT_KEY_PREFIX.getValue() + postId + ":" + email;
    }

    public Long getPostIdFromPostViewCountRedisKey(String key) {
        return Long.parseLong(key.substring(VIEW_COUNT_KEY_PREFIX.getValue().length()));
    }

    public String generateCacheKey(String keyPattern, Object[] args) {
        for (int i = 0; i < args.length; i++) {
            // 키 패턴에 {i}가 포함된 경우에만 해당 인덱스의 파라미터를 삽입
            if (keyPattern.contains("{" + i + "}")) {
                String replacement = (args[i] != null) ? args[i].toString() : "null";
                keyPattern = keyPattern.replace("{" + i + "}", replacement);
            }
        }
        return keyPattern;
    }

    public String getCreateLockKey(String key) {
        return CREATE_LOCK_PREFIX.getValue() + key;
    }

    public String getRefreshLockKey(String key) {
        return REFRESH_LOCK_PREFIX.getValue() + key;
    }

    public boolean isCacheExpiringSoon(String key, Long defaultTtl, Double percent) {
        Long leftTtl = redisTemplate.getExpire(key);
        return defaultTtl != null && ((double) leftTtl / defaultTtl) * 100 < percent;
    }
}
