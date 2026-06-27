package com.example.solidconnection.cache;

import static com.example.solidconnection.redis.RedisConstants.CREATE_LOCK_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.cache.annotation.ThunderingHerdCaching;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.util.RedisUtils;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@TestContainerSpringBootTest
@DisplayName("ThunderingHerdCaching Aspect 테스트")
class ThunderingHerdCachingAspectTest {

    private static final String CACHE_KEY_PREFIX = "test:thundering:";
    private static final long TEST_CACHE_TTL_SEC = 20L;

    @Autowired
    private TestCacheService testCacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @BeforeEach
    void setUp() {
        testCacheService.reset();
    }

    @Test
    void 캐시가_만료_임박하면_갱신락을_획득한_요청이_값을_다시_계산한다() {
        // given
        String firstValue = testCacheService.getValue("refresh");
        redisTemplate.expire(cacheKey("refresh"), Duration.ofSeconds(1));

        // when
        String secondValue = testCacheService.getValue("refresh");

        // then
        assertAll(
                () -> assertThat(firstValue).isEqualTo("value-1"),
                () -> assertThat(secondValue).isEqualTo("value-2"),
                () -> assertThat(redisTemplate.opsForValue().get(cacheKey("refresh"))).isEqualTo("value-2"),
                () -> assertThat(testCacheService.getCallCount()).isEqualTo(2)
        );
    }

    @Test
    void 캐시가_만료_임박했지만_갱신락_획득에_실패하면_기존_캐시값을_반환한다() {
        // given
        String firstValue = testCacheService.getValue("locked");
        redisTemplate.expire(cacheKey("locked"), Duration.ofSeconds(1));
        redisTemplate.opsForValue().set(createLockKey("locked"), "lock", Duration.ofSeconds(5));

        // when
        String secondValue = testCacheService.getValue("locked");

        // then
        assertAll(
                () -> assertThat(secondValue).isEqualTo(firstValue),
                () -> assertThat(redisTemplate.opsForValue().get(cacheKey("locked"))).isEqualTo(firstValue),
                () -> assertThat(testCacheService.getCallCount()).isEqualTo(1),
                () -> assertThat(testCacheService.getInvocationCount()).isEqualTo(1)
        );
    }

    @Test
    void 캐시_갱신_중_오류가_발생하면_기존_캐시값을_반환하고_TTL을_연장하지_않는다() {
        // given
        String firstValue = testCacheService.getValue("failed");
        redisTemplate.expire(cacheKey("failed"), Duration.ofSeconds(1));
        testCacheService.failWithRuntimeException();

        // when
        String secondValue = testCacheService.getValue("failed");

        // then
        Long ttlMillis = redisTemplate.getExpire(cacheKey("failed"), TimeUnit.MILLISECONDS);
        assertAll(
                () -> assertThat(secondValue).isEqualTo(firstValue),
                () -> assertThat(redisTemplate.opsForValue().get(cacheKey("failed"))).isEqualTo(firstValue),
                () -> assertThat(testCacheService.getCallCount()).isEqualTo(1),
                () -> assertThat(testCacheService.getInvocationCount()).isEqualTo(2),
                () -> assertThat(ttlMillis).isLessThan(TEST_CACHE_TTL_SEC * 1000)
        );
    }

    @Test
    void 캐시_갱신_중_CustomException이_발생하면_기존_캐시값으로_fallback하지_않고_예외를_전파한다() {
        // given
        testCacheService.getValue("custom-exception");
        redisTemplate.expire(cacheKey("custom-exception"), Duration.ofSeconds(1));
        testCacheService.failWithCustomException();

        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> testCacheService.getValue("custom-exception"))
                        .isInstanceOf(CustomException.class),
                () -> assertThat(testCacheService.getCallCount()).isEqualTo(1),
                () -> assertThat(testCacheService.getInvocationCount()).isEqualTo(2)
        );
    }

    @Test
    void 만료_시간이_없는_캐시는_만료_임박으로_판단하지_않고_기존_캐시값을_반환한다() {
        // given
        String firstValue = testCacheService.getValue("no-expire");
        redisTemplate.persist(cacheKey("no-expire"));

        // when
        String secondValue = testCacheService.getValue("no-expire");

        // then
        assertAll(
                () -> assertThat(secondValue).isEqualTo(firstValue),
                () -> assertThat(testCacheService.getCallCount()).isEqualTo(1),
                () -> assertThat(testCacheService.getInvocationCount()).isEqualTo(1),
                () -> assertThat(redisTemplate.getExpire(cacheKey("no-expire"))).isEqualTo(-1)
        );
    }

    @Test
    void 만료_임박_비율이_유효하지_않으면_만료_임박으로_판단하지_않는다() {
        // given
        String key = cacheKey("invalid-percent");
        redisTemplate.opsForValue().set(key, "value", Duration.ofSeconds(1));

        // when & then
        assertAll(
                () -> assertThat(redisUtils.isCacheExpiringSoon(key, TEST_CACHE_TTL_SEC, 0.0)).isFalse(),
                () -> assertThat(redisUtils.isCacheExpiringSoon(key, TEST_CACHE_TTL_SEC, -1.0)).isFalse(),
                () -> assertThat(redisUtils.isCacheExpiringSoon(key, TEST_CACHE_TTL_SEC, 101.0)).isFalse()
        );
    }

    @Test
    void 캐시_갱신_중_기존_캐시가_만료되어도_생성락으로_중복_계산을_막는다() throws Exception {
        // given
        String firstValue = testCacheService.getValue("expired-during-refresh");
        redisTemplate.expire(cacheKey("expired-during-refresh"), Duration.ofSeconds(1));
        testCacheService.blockNextInvocation();

        ExecutorService refreshExecutor = Executors.newSingleThreadExecutor();
        ExecutorService waitingExecutor = Executors.newSingleThreadExecutor();

        try {
            Future<String> refreshResult = refreshExecutor.submit(() -> testCacheService.getValue("expired-during-refresh"));
            testCacheService.awaitBlockedInvocation();
            await().atMost(Duration.ofSeconds(3))
                    .untilAsserted(() -> assertThat(redisTemplate.hasKey(cacheKey("expired-during-refresh"))).isFalse());

            // when
            Future<String> waitingResult = waitingExecutor.submit(() -> testCacheService.getValue("expired-during-refresh"));
            await().during(Duration.ofMillis(200))
                    .atMost(Duration.ofSeconds(1))
                    .untilAsserted(() -> assertThat(waitingResult.isDone()).isFalse());
            testCacheService.releaseBlockedInvocation();

            // then
            assertAll(
                    () -> assertThat(firstValue).isEqualTo("value-1"),
                    () -> assertThat(refreshResult.get(3, TimeUnit.SECONDS)).isEqualTo("value-2"),
                    () -> assertThat(waitingResult.get(3, TimeUnit.SECONDS)).isEqualTo("value-2"),
                    () -> assertThat(redisTemplate.opsForValue().get(cacheKey("expired-during-refresh"))).isEqualTo("value-2"),
                    () -> assertThat(testCacheService.getCallCount()).isEqualTo(2),
                    () -> assertThat(testCacheService.getInvocationCount()).isEqualTo(2)
            );
        } finally {
            testCacheService.releaseBlockedInvocation();
            refreshExecutor.shutdownNow();
            waitingExecutor.shutdownNow();
        }
    }

    private String cacheKey(String key) {
        return CACHE_KEY_PREFIX + key;
    }

    private String createLockKey(String key) {
        return CREATE_LOCK_PREFIX.getValue() + cacheKey(key);
    }

    @TestConfiguration
    static class TestCacheConfig {

        @Bean
        TestCacheService testCacheService() {
            return new TestCacheService();
        }
    }

    static class TestCacheService {

        private final AtomicInteger callCount = new AtomicInteger();
        private final AtomicInteger invocationCount = new AtomicInteger();
        private volatile boolean failWithRuntimeException;
        private volatile boolean failWithCustomException;
        private volatile boolean blockNextInvocation;
        private volatile CountDownLatch blockedInvocationStarted;
        private volatile CountDownLatch blockedInvocationRelease;

        @ThunderingHerdCaching(
                key = CACHE_KEY_PREFIX + "{0}",
                cacheManager = "customCacheManager",
                ttlSec = TEST_CACHE_TTL_SEC
        )
        public String getValue(String key) {
            invocationCount.incrementAndGet();
            awaitIfBlocked();
            if (failWithCustomException) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            if (failWithRuntimeException) {
                throw new IllegalStateException("refresh failed");
            }
            return "value-" + callCount.incrementAndGet();
        }

        void reset() {
            callCount.set(0);
            invocationCount.set(0);
            failWithRuntimeException = false;
            failWithCustomException = false;
            blockNextInvocation = false;
            blockedInvocationStarted = null;
            blockedInvocationRelease = null;
        }

        int getCallCount() {
            return callCount.get();
        }

        int getInvocationCount() {
            return invocationCount.get();
        }

        void failWithRuntimeException() {
            failWithRuntimeException = true;
        }

        void failWithCustomException() {
            failWithCustomException = true;
        }

        void blockNextInvocation() {
            blockedInvocationStarted = new CountDownLatch(1);
            blockedInvocationRelease = new CountDownLatch(1);
            blockNextInvocation = true;
        }

        void awaitBlockedInvocation() throws InterruptedException {
            assertThat(blockedInvocationStarted.await(3, TimeUnit.SECONDS)).isTrue();
        }

        void releaseBlockedInvocation() {
            if (blockedInvocationRelease != null) {
                blockedInvocationRelease.countDown();
            }
        }

        private void awaitIfBlocked() {
            if (!blockNextInvocation) {
                return;
            }
            blockNextInvocation = false;
            blockedInvocationStarted.countDown();
            try {
                blockedInvocationRelease.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }
}
