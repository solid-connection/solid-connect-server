package com.example.solidconnection.concurrency;

import com.example.solidconnection.application.service.ApplicationQueryService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
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
public class ThunderingHerdTest {
    @Autowired
    private ApplicationQueryService applicationQueryService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private SiteUserRepository siteUserRepository;
    private int THREAD_NUMS = 1000;
    private int THREAD_POOL_SIZE = 200;
    private int TIMEOUT_SECONDS = 10;
    private SiteUser siteUser;

    @BeforeEach
    public void setUp() {
        siteUser = createSiteUser();
        siteUserRepository.save(siteUser);
    }

    private SiteUser createSiteUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
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
                            () -> applicationQueryService.getApplicants(siteUser, "", ""),
                            () -> applicationQueryService.getApplicants(siteUser, "ASIA", ""),
                            () -> applicationQueryService.getApplicants(siteUser, "", "추오")
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
