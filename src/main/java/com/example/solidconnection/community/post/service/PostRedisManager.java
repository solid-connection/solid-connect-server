package com.example.solidconnection.community.post.service;

import static com.example.solidconnection.redis.RedisConstants.POST_CREATE_PREFIX;
import static com.example.solidconnection.redis.RedisConstants.VALIDATE_POST_CREATE_TTL;
import static com.example.solidconnection.redis.RedisConstants.VALIDATE_VIEW_COUNT_KEY_PREFIX;
import static com.example.solidconnection.redis.RedisConstants.VALIDATE_VIEW_COUNT_TTL;
import static com.example.solidconnection.redis.RedisConstants.VIEW_COUNT_KEY_PREFIX;

import com.example.solidconnection.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostRedisManager {

    private final RedisService redisService;

    public Long getPostIdFromPostViewCountRedisKey(String key) {
        return Long.parseLong(key.substring(VIEW_COUNT_KEY_PREFIX.getValue().length()));
    }

    public Long getAndDeleteViewCount(String key) {
        return redisService.getAndDelete(key);
    }

    public void deleteViewCountCache(Long postId) {
        String key = getPostViewCountRedisKey(postId);
        redisService.deleteKey(key);
    }

    public void incrementViewCountIfFirstAccess(long siteUserId, Long postId) {
        String validateKey = getValidatePostViewCountRedisKey(siteUserId, postId);
        boolean isFirstAccess = redisService.isPresent(validateKey, VALIDATE_VIEW_COUNT_TTL.getValue());

        if (isFirstAccess) {
            String viewCountKey = getPostViewCountRedisKey(postId);
            redisService.increaseViewCount(viewCountKey);
        }
    }

    public String getPostViewCountRedisKey(Long postId) {
        return VIEW_COUNT_KEY_PREFIX.getValue() + postId;
    }

    public String getValidatePostViewCountRedisKey(long siteUserId, Long postId) {
        return VALIDATE_VIEW_COUNT_KEY_PREFIX.getValue() + postId + ":" + siteUserId;
    }

    public boolean isPostCreationAllowed(Long siteUserId) {
        String key = getPostCreateRedisKey(siteUserId);
        return redisService.isPresent(key, VALIDATE_POST_CREATE_TTL.getValue());
    }

    public String getPostCreateRedisKey(Long siteUserId) {
        return POST_CREATE_PREFIX.getValue() + siteUserId;
    }
}
