package com.example.solidconnection.cache.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component("customCacheManager")
public class CustomCacheManager implements CacheManager {
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CustomCacheManager(RedisTemplate<String, Object> redisTemplate) {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        this.redisTemplate = redisTemplate;
    }

    public void put(String key, Object object, Long ttl) {
        redisTemplate.opsForValue().set(key, object, Duration.ofSeconds(ttl));
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void evict(String key) {
        redisTemplate.delete(key);
    }

    public void evictUsingPrefix(String key) {
        Set<String> keys = redisTemplate.keys(key + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
