package com.example.solidconnection.community.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.board.fixture.BoardFixture;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.community.post.fixture.PostFixture;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.redis.RedisService;
import com.example.solidconnection.scheduler.UpdateViewCountScheduler;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

@TestContainerSpringBootTest
@DisplayName("기존 조회수 집계 유실 재현 테스트")
class LegacyViewCountFailureTest {

    @Autowired
    private PostFixture postFixture;

    @Autowired
    private BoardFixture boardFixture;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private UpdateViewCountScheduler scheduler;

    @Nested
    @DisplayName("기존 조회수 집계의 유실 재현")
    class 기존_구현의_장애_재현 {

        @Test
        void 기존_구현에서_DB_실패_5건의_조회수_500회가_유실된다() {
            // given: 게시글 150개 중 50개에 각각 100회 조회
            Board board = boardFixture.자유게시판();
            SiteUser user = siteUserFixture.사용자();
            List<Post> posts = new ArrayList<>();
            for (int i = 0; i < 150; i++) {
                posts.add(postFixture.게시글("title", "content", false, PostCategory.자유, board, user));
            }
            for (int i = 0; i < 50; i++) {
                for (int count = 0; count < 100; count++) {
                    redisService.increaseViewCount("post:view:" + posts.get(i).getId());
                }
            }
            PostRepository failingRepository = mock(PostRepository.class);
            doThrow(new IllegalStateException("injected DB failure"))
                    .when(failingRepository).increaseViewCount(anyLong(), anyLong());
            // Frozen copy of the pre-change GETDEL -> DB behavior, measured before implementation.

            // when: 첫 5개 DB 실패 후 같은 작업 재시도
            for (int i = 0; i < 50; i++) {
                String key = "post:view:" + posts.get(i).getId();
                if (i < 5) {
                    assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> legacyFlush(key, failingRepository)))
                            .isInstanceOf(IllegalStateException.class);
                }
                transactionTemplate.executeWithoutResult(status -> legacyFlush(key, postRepository));
            }

            // then: 총합과 게시글별 결과 모두 검증
            long total = 0;
            for (int i = 0; i < 150; i++) {
                long actual = postRepository.getById(posts.get(i).getId()).getViewCount();
                assertThat(actual).isEqualTo(i >= 5 && i < 50 ? 100 : 0);
                total += actual;
            }
            assertThat(total).isEqualTo(4500);
            System.out.println("BASELINE input=5000 persisted=" + total + " lost=" + (5000 - total));
        }

        private void legacyFlush(String key, PostRepository repository) {
            long postId = Long.parseLong(key.substring("post:view:".length()));
            Long delta = redisService.getAndDelete(key);
            if (delta != null) {
                repository.increaseViewCount(postId, delta);
            }
        }
    }
}
