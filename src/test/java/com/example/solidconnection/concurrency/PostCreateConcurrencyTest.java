package com.example.solidconnection.concurrency;

import static com.example.solidconnection.redis.RedisConstants.VALIDATE_POST_CREATE_TTL;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.solidconnection.community.post.service.PostRedisManager;
import com.example.solidconnection.redis.RedisService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("게시글 생성 동시성 테스트")
class PostCreateConcurrencyTest {

    @Autowired
    private RedisService redisService;

    @Autowired
    private PostRedisManager postRedisManager;

    @Autowired
    private SiteUserFixture siteUserFixture;

    private SiteUser user;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
        redisService.deleteKey(postRedisManager.getPostCreateRedisKey(user.getId()));
    }

    @Test
    void 동시에_여러_요청이_들어오면_첫_번째_요청만_허용된다() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch readyLatch = new CountDownLatch(5);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(5);

        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger deniedCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    boolean isAllowed = postRedisManager.isPostCreationAllowed(user.getId());
                    if (isAllowed) {
                        allowedCount.incrementAndGet();
                    } else {
                        deniedCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS); //모든 스레드가 준비 상태가 될 때까지 대기
        startLatch.countDown(); //동시 실행
        doneLatch.await(5, TimeUnit.SECONDS); //모든 스레드의 작업이 끝날 때까지 대기
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // then
        assertThat(allowedCount.get()).isEqualTo(1);
        assertThat(deniedCount.get()).isEqualTo(4);
    }

    @Test
    void TTL이_지나면_다시_게시글_생성이_허용된다() throws InterruptedException {
        // given
        boolean firstAttempt = postRedisManager.isPostCreationAllowed(user.getId());
        boolean secondAttemptBeforeTtl = postRedisManager.isPostCreationAllowed(user.getId());

        // when
        long ttlSeconds = Long.parseLong(VALIDATE_POST_CREATE_TTL.getValue());
        Thread.sleep((ttlSeconds + 1) * 1000);

        boolean attemptAfterTtl = postRedisManager.isPostCreationAllowed(user.getId());

        // then
        assertThat(firstAttempt).isTrue();
        assertThat(secondAttemptBeforeTtl).isFalse();
        assertThat(attemptAfterTtl).isTrue();
    }

    @Test
    void 서로_다른_사용자는_동시에_게시글을_생성할_수_있다() throws InterruptedException {
        // given
        SiteUser user1 = siteUserFixture.사용자(1, "사용자1");
        SiteUser user2 = siteUserFixture.사용자(2, "사용자2");
        SiteUser user3 = siteUserFixture.사용자(3, "사용자3");

        redisService.deleteKey(postRedisManager.getPostCreateRedisKey(user1.getId()));
        redisService.deleteKey(postRedisManager.getPostCreateRedisKey(user2.getId()));
        redisService.deleteKey(postRedisManager.getPostCreateRedisKey(user3.getId()));

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch readyLatch = new CountDownLatch(3);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(3);

        AtomicInteger allowedCount = new AtomicInteger(0);

        // when
        for (SiteUser currentUser : new SiteUser[]{user1, user2, user3}) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    boolean isAllowed = postRedisManager.isPostCreationAllowed(currentUser.getId());
                    if (isAllowed) {
                        allowedCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS); //모든 스레드가 준비 상태가 될 때까지 대기
        startLatch.countDown(); //동시 실행
        doneLatch.await(5, TimeUnit.SECONDS); //모든 스레드의 작업이 끝날 때까지 대기
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // then
        assertThat(allowedCount.get()).isEqualTo(3);
    }
}
