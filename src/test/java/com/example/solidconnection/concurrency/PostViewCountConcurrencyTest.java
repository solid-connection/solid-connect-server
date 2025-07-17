package com.example.solidconnection.concurrency;

import static com.example.solidconnection.community.post.service.RedisConstants.VALIDATE_VIEW_COUNT_TTL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.board.repository.BoardRepository;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.community.post.service.RedisService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.util.RedisUtils;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@TestContainerSpringBootTest
@DisplayName("게시글 조회수 동시성 테스트")
class PostViewCountConcurrencyTest {

    @Autowired
    private RedisService redisService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Value("${view.count.scheduling.delay}")
    private int SCHEDULING_DELAY_MS;

    private int THREAD_NUMS = 1000;
    private int THREAD_POOL_SIZE = 200;
    private int TIMEOUT_SECONDS = 10;

    private Post post;
    private Board board;
    private SiteUser user;

    @BeforeEach
    void setUp() {
        board = createBoard();
        boardRepository.save(board);
        user = siteUserFixture.사용자();
        post = createPost(board, user);
        postRepository.save(post);
    }

    private Board createBoard() {
        return new Board(
                "FREE", "자유게시판");
    }

    private Post createPost(Board board, SiteUser siteUser) {
        Post post = new Post(
                "title",
                "content",
                false,
                0L,
                0L,
                PostCategory.valueOf("자유")
        );
        post.setBoardAndSiteUserId(board.getCode(), siteUser.getId());

        return post;
    }

    @Test
    void 게시글을_조회할_때_조회수_동시성_문제를_해결한다() throws InterruptedException {

        redisService.deleteKey(redisUtils.getValidatePostViewCountRedisKey(user.getId(), post.getId()));

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch doneSignal = new CountDownLatch(THREAD_NUMS);

        for (int i = 0; i < THREAD_NUMS; i++) {
            executorService.submit(() -> {
                try {
                    redisService.increaseViewCount(redisUtils.getPostViewCountRedisKey(post.getId()));
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

        Thread.sleep(SCHEDULING_DELAY_MS + 1000);

        assertEquals(THREAD_NUMS, postRepository.getById(post.getId()).getViewCount());
    }

    @Test
    void 게시글을_조회할_때_조회수_조작_문제를_해결한다() throws InterruptedException {

        redisService.deleteKey(redisUtils.getValidatePostViewCountRedisKey(user.getId(), post.getId()));

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch doneSignal = new CountDownLatch(THREAD_NUMS);

        for (int i = 0; i < THREAD_NUMS; i++) {
            executorService.submit(() -> {
                try {
                    boolean isFirstTime = redisService.isPresent(redisUtils.getValidatePostViewCountRedisKey(user.getId(), post.getId()));
                    if (isFirstTime) {
                        redisService.increaseViewCount(redisUtils.getPostViewCountRedisKey(post.getId()));
                    }
                } finally {
                    doneSignal.countDown();
                }
            });
        }
        Thread.sleep(Long.parseLong(VALIDATE_VIEW_COUNT_TTL.getValue()) * 1000);
        for (int i = 0; i < THREAD_NUMS; i++) {
            executorService.submit(() -> {
                try {
                    boolean isFirstTime = redisService.isPresent(redisUtils.getValidatePostViewCountRedisKey(user.getId(), post.getId()));
                    if (isFirstTime) {
                        redisService.increaseViewCount(redisUtils.getPostViewCountRedisKey(post.getId()));
                    }
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

        Thread.sleep(SCHEDULING_DELAY_MS + 1000);

        assertEquals(2L, postRepository.getById(post.getId()).getViewCount());
    }
}
