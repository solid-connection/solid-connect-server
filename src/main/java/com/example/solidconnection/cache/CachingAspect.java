package com.example.solidconnection.cache;

import com.example.solidconnection.cache.annotation.DefaultCacheOut;
import com.example.solidconnection.cache.annotation.DefaultCaching;
import com.example.solidconnection.cache.manager.CacheManager;
import com.example.solidconnection.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class CachingAspect {

    private final ApplicationContext applicationContext;
    private final RedisUtils redisUtils;

    @Around("@annotation(defaultCaching)")
    public Object cache(ProceedingJoinPoint joinPoint, DefaultCaching defaultCaching) throws Throwable {

        CacheManager cacheManager = (CacheManager) applicationContext.getBean(defaultCaching.cacheManager());
        String key = redisUtils.generateCacheKey(defaultCaching.key(), joinPoint.getArgs());
        Long ttl = defaultCaching.ttlSec();

        // 1. 캐시에 있으면 반환
        Object cachedValue = cacheManager.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        // 2. 캐시에 없으면 캐싱 후 반환
        Object result = joinPoint.proceed();
        cacheManager.put(key, result, ttl);
        return result;
    }

    @Around("@annotation(defaultCacheOut)")
    public Object cacheEvict(ProceedingJoinPoint joinPoint, DefaultCacheOut defaultCacheOut) throws Throwable {

        CacheManager cacheManager = (CacheManager) applicationContext.getBean(defaultCacheOut.cacheManager());

        for (String key : defaultCacheOut.key()) {
            String cacheKey = redisUtils.generateCacheKey(key, joinPoint.getArgs());
            boolean usingPrefix = defaultCacheOut.prefix();

            if (usingPrefix) {
                cacheManager.evictUsingPrefix(cacheKey);
            } else {
                cacheManager.evict(cacheKey);
            }
        }
        return joinPoint.proceed();
    }
}
