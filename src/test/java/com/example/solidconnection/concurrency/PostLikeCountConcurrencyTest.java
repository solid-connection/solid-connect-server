package com.example.solidconnection.concurrency;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.board.fixture.BoardFixture;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.community.post.fixture.PostFixture;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.community.post.service.PostLikeService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestContainerSpringBootTest
@DisplayName("게시글 좋아요 동시성 테스트")
class PostLikeCountConcurrencyTest {

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private BoardFixture boardFixture;

    @Autowired
    private PostFixture postFixture;

    private int THREAD_NUMS = 1000;
    private int THREAD_POOL_SIZE = 200;
    private int TIMEOUT_SECONDS = 10;

    private Post post;
    private Board board;
    private SiteUser user;

    @BeforeEach
    void setUp() {
        board = boardFixture.자유게시판();
        user = siteUserFixture.사용자();
        post = postFixture.게시글(
                "title",
                "content",
                false,
                PostCategory.자유,
                board,
                user);
    }

    @Test
    void 게시글_좋아요_동시성_문제를_해결한다() throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch doneSignal = new CountDownLatch(THREAD_NUMS);

        Long likeCount = postRepository.getById(post.getId()).getLikeCount();

        for (int i = 0; i < THREAD_NUMS; i++) {
            String nickname = "nickname" + i;
            SiteUser tmpSiteUser = siteUserFixture.사용자(i, nickname);
            executorService.submit(() -> {
                try {
                    postLikeService.likePost(tmpSiteUser, post.getId());
                    postLikeService.dislikePost(tmpSiteUser, post.getId());
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

        assertEquals(likeCount, postRepository.getById(post.getId()).getLikeCount());
    }
}
