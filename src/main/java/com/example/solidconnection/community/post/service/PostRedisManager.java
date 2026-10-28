package com.example.solidconnection.community.post.service;

import static com.example.solidconnection.redis.RedisConstants.POST_CREATE_PREFIX;
import static com.example.solidconnection.redis.RedisConstants.VALIDATE_POST_CREATE_TTL;
import static com.example.solidconnection.redis.RedisConstants.VALIDATE_VIEW_COUNT_KEY_PREFIX;
import static com.example.solidconnection.redis.RedisConstants.VALIDATE_VIEW_COUNT_TTL;

import com.example.solidconnection.community.post.repository.ViewCountStreamRepository;
import com.example.solidconnection.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostRedisManager {

    private final RedisService redisService;
    private final ViewCountStreamRepository streamRepository;

    public void deleteViewCountCache(Long postId) {
        streamRepository.deleteActive(postId);
    }

    public void incrementViewCountIfFirstAccess(long siteUserId, Long postId) {
        String validateKey = getValidatePostViewCountRedisKey(siteUserId, postId);
        boolean isFirstAccess = redisService.isPresent(validateKey, VALIDATE_VIEW_COUNT_TTL.getValue());

        if (isFirstAccess) {
            streamRepository.increment(postId);
        }
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
