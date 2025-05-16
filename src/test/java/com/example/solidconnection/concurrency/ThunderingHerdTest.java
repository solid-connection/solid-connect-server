package com.example.solidconnection.concurrency;

import com.example.solidconnection.application.service.ApplicationQueryService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.siteuser.domain.PreparationStatus;
import com.example.solidconnection.siteuser.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@TestContainerSpringBootTest
@DisplayName("ThunderingHerd 테스트")
class ThunderingHerdTest {

    @Autowired
    private ApplicationQueryService applicationQueryService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SiteUserFixture siteUserFixture;

    private int THREAD_NUMS = 1000;
    private int THREAD_POOL_SIZE = 200;
    private int TIMEOUT_SECONDS = 10;
    private SiteUser user;

    @BeforeEach
    public void setUp() {
        user = siteUserFixture.사용자();
    }

    @Test
    public void ThunderingHerd_문제를_해결한다() throws InterruptedException {
        redisTemplate.opsForValue().getAndDelete("application::");
        redisTemplate.opsForValue().getAndDelete("application:ASIA:");
        redisTemplate.opsForValue().getAndDelete("application::추오");

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch doneSignal = new CountDownLatch(THREAD_NUMS);

        for (int i = 0; i < THREAD_NUMS; i++) {
            executorService.submit(() -> {
                try {
                    List<Runnable> tasks = Arrays.asList(
                            () -> applicationQueryService.getApplicants(user, "", ""),
                            () -> applicationQueryService.getApplicants(user, "ASIA", ""),
                            () -> applicationQueryService.getApplicants(user, "", "추오")
                    );
                    Collections.shuffle(tasks);
                    tasks.forEach(Runnable::run);
                } finally {
                    doneSignal.countDown();
                }
            });
        }

        doneSignal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!terminated) {
            System.err.println("ExecutorService did not terminate in the expected time.");
        }
    }
}
