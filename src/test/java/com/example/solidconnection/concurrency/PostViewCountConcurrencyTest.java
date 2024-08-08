package com.example.solidconnection.concurrency;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.service.RedisService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("게시글 조회수 동시성 테스트")
public class PostViewCountConcurrencyTest {

    @Autowired
    private RedisService redisService;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private SiteUserRepository siteUserRepository;

    @Value("${view.count.scheduling.delay}")
    private int SCHEDULING_DELAY_MS;
    private int THREAD_NUMS = 1000;
    private int THREAD_POOL_SIZE = 200;
    private int TIMEOUT_SECONDS = 10;

    private Post post;
    private Post postToBeDeleted;
    private Board board;
    private SiteUser siteUser;

    @BeforeEach
    public void setUp() {
        board = createBoard();
        boardRepository.save(board);
        siteUser = createSiteUser();
        siteUserRepository.save(siteUser);
        post = createPost(board, siteUser);
        postRepository.save(post);
        postToBeDeleted = createPost(board, siteUser);
        postRepository.save(postToBeDeleted);
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
        post.setBoardAndSiteUser(board, siteUser);

        return post;
    }

    @Test
    public void 게시글을_조회할_때_조회수_동시성_문제를_해결한다() throws InterruptedException {
        Thread.sleep(SCHEDULING_DELAY_MS);
        long startTime = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch doneSignal = new CountDownLatch(THREAD_NUMS);

        for (int i = 0; i < THREAD_NUMS; i++) {
            executorService.submit(() -> {
                try {
                    redisService.increaseViewCountSync(post.getId());
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

        Thread.sleep(SCHEDULING_DELAY_MS);

        long endTime = System.currentTimeMillis();
        long runningTime = endTime - startTime - SCHEDULING_DELAY_MS;
        System.out.println("runningTime: " + runningTime + "ms");

        Long viewCount = postRepository.getById(post.getId()).getViewCount();
        assertEquals(THREAD_NUMS, viewCount);
    }
}
