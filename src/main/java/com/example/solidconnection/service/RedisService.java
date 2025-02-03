package com.example.solidconnection.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.example.solidconnection.type.RedisConstants.VALIDATE_VIEW_COUNT_TTL;
import static com.example.solidconnection.type.RedisConstants.VIEW_COUNT_TTL;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> incrViewCountLuaScript;

    @Autowired
    public RedisService(RedisTemplate<String, String> redisTemplate,
                        @Qualifier("incrViewCountScript") RedisScript<Long> incrViewCountLuaScript) {
        this.redisTemplate = redisTemplate;
        this.incrViewCountLuaScript = incrViewCountLuaScript;
    }

    // incr & set ttl -> lua
    public void increaseViewCount(String key) {
        redisTemplate.execute(incrViewCountLuaScript, Collections.singletonList(key), VIEW_COUNT_TTL.getValue());
    }

    public void deleteKey(String key) {
        redisTemplate.opsForValue().getAndDelete(key);
    }

    public Long getAndDelete(String key) {
        return Long.valueOf(redisTemplate.opsForValue().getAndDelete(key));
    }

    public boolean isPresent(String key) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Long.parseLong(VALIDATE_VIEW_COUNT_TTL.getValue()), TimeUnit.SECONDS));
    }

    public boolean isKeyExists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
