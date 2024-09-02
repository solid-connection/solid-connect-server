package com.example.solidconnection.cache;

import com.example.solidconnection.cache.annotation.ThunderingHerdCaching;
import com.example.solidconnection.cache.manager.CacheManager;
import com.example.solidconnection.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;

import static com.example.solidconnection.type.RedisConstants.*;

@Aspect
@Component
@Slf4j
public class ThunderingHerdCachingAspect {
    private final ApplicationContext applicationContext;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CompletableFutureManager futureManager;
    private final RedisUtils redisUtils;

    @Autowired
    public ThunderingHerdCachingAspect(ApplicationContext applicationContext, RedisTemplate<String, Object> redisTemplate,
                                       CompletableFutureManager futureManager, RedisUtils redisUtils) {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        this.redisTemplate = redisTemplate;
        this.applicationContext = applicationContext;
        this.futureManager = futureManager;
        this.redisUtils = redisUtils;
    }

    @Around("@annotation(thunderingHerdCaching)")
    public Object cache(ProceedingJoinPoint joinPoint, ThunderingHerdCaching thunderingHerdCaching) {

        CacheManager cacheManager = (CacheManager) applicationContext.getBean(thunderingHerdCaching.cacheManager());
        String key = redisUtils.generateCacheKey(thunderingHerdCaching.key(), joinPoint.getArgs());
        Long ttl = thunderingHerdCaching.ttlSec();

        Object cachedValue = cacheManager.get(key);
        if (cachedValue == null) {
            log.info("Cache miss. Key: {}, Thread: {}", key, Thread.currentThread().getName());
            return createCache(joinPoint, cacheManager, ttl, key);
        }

        if (redisUtils.isCacheExpiringSoon(key, ttl, Double.valueOf(REFRESH_LIMIT_PERCENT.getValue()))) {
            log.info("Cache hit, but TTL is expiring soon. Key: {}, Thread: {}", key, Thread.currentThread().getName());
            return refreshCache(cachedValue, ttl, key);
        }

        log.info("Cache hit. Key: {}, Thread: {}", key, Thread.currentThread().getName());
        return cachedValue;
    }

    private Object createCache(ProceedingJoinPoint joinPoint, CacheManager cacheManager, Long ttl, String key) {
        return executeWithLock(
                redisUtils.getCreateLockKey(key),
                () -> {
                    log.info("생성락 흭득하였습니다. Key: {}, Thread: {}", key, Thread.currentThread().getName());
                    Object result = proceedJoinPoint(joinPoint);
                    cacheManager.put(key, result, ttl);
                    redisTemplate.convertAndSend(CREATE_CHANNEL.getValue(), key);
                    log.info("캐시 생성 후 채널에 pub 진행합니다. Key: {}, Thread: {}", key, Thread.currentThread().getName());
                    return result;
                },
                () -> {
                    log.info("생성락 흭득에 실패하여 대기하러 갑니다. Key: {}, Thread: {}", key, Thread.currentThread().getName());
                    return waitForCacheToUpdate(joinPoint, key);
                }
        );
    }

    private Object refreshCache(Object cachedValue, Long ttl, String key) {
        return executeWithLock(
                redisUtils.getRefreshLockKey(key),
                () -> {
                    log.info("갱신락 흭득하였습니다. Key: {}, Thread: {}", key, Thread.currentThread().getName());
                    redisTemplate.opsForValue().getAndExpire(key, Duration.ofSeconds(ttl));
                    log.info("TTL 갱신을 마쳤습니다. Key: {}, Thread: {}", key, Thread.currentThread().getName());
                    return cachedValue;
                },
                () -> {
                    log.info("갱신락 흭득에 실패하였습니다. 캐시의 값을 바로 반환합니다. Key: {}, Thread: {}", key, Thread.currentThread().getName());
                    return cachedValue;
                }
        );
    }

    private Object executeWithLock(String lockKey, Callable<Object> onLockAcquired, Callable<Object> onLockFailed) {
        String lockValue = UUID.randomUUID().toString();
        boolean lockAcquired = false;

        try {
            lockAcquired = tryAcquireLock(lockKey, lockValue);
            if (lockAcquired) {
                return onLockAcquired.call();
            } else {
                return onLockFailed.call();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during executeWithLock", e);
        } finally {
            releaseLock(lockKey, lockValue, lockAcquired);
        }
    }

    private boolean tryAcquireLock(String lockKey, String lockValue) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofMillis(Long.parseLong(LOCK_TIMEOUT_MS.getValue())));
    }

    private void releaseLock(String lockKey, String lockValue, boolean lockAcquired) {
        if (lockAcquired && lockValue.equals(redisTemplate.opsForValue().get(lockKey))) {
            redisTemplate.delete(lockKey);
            log.info("락 반환합니다. Key: {}", lockKey);
        }
    }

    private Object waitForCacheToUpdate(ProceedingJoinPoint joinPoint, String key) {
        CompletableFuture<Void> future = futureManager.getOrCreateFuture(key);
        try {
            future.get(Long.parseLong(MAX_WAIT_TIME_MS.getValue()), TimeUnit.MILLISECONDS);
            log.info("대기에서 빠져나와 생성된 캐시값을 가져옵니다. Key: {}, Thread: {}", key, Thread.currentThread().getName());
            return redisTemplate.opsForValue().get(key);
        } catch (TimeoutException e) {
            log.warn("대기중 타임아웃 발생하여 DB 접근하여 반환합니다. Key: {}, Thread: {}", key, Thread.currentThread().getName());
            return proceedJoinPoint(joinPoint);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error during waitForCacheToUpdate", e);
        }
    }

    private Object proceedJoinPoint(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException("Error during proceedJoinPoint", e);
        }
    }
}
