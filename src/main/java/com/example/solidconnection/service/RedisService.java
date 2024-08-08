package com.example.solidconnection.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static com.example.solidconnection.type.RedisConstants.*;

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
    public void increaseViewCountSync(Long postId) {
        String key = getPostKey(postId);
        redisTemplate.execute(incrViewCountLuaScript, Collections.singletonList(key), VIEW_COUNT_TTL.getValue());
    }

    public void deletePostViewCountKey(Long postId) {
        String key = getPostKey(postId);
        redisTemplate.opsForValue().getAndDelete(key);
    }

    public Long getViewCountValueAndDeleteKey(String key) {
        return Long.valueOf(redisTemplate.opsForValue().getAndDelete(key));
    }

    private String getPostKey(Long postId) {
        return VIEW_COUNT_KEY_PREFIX.getValue() + postId;
    }
}
