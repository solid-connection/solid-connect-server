package com.example.solidconnection.news.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsListResponse;
import com.example.solidconnection.news.dto.NewsResponse;
import com.example.solidconnection.news.fixture.LikedNewsFixture;
import com.example.solidconnection.news.fixture.NewsFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("소식지 조회 서비스 테스트")
class NewsQueryServiceTest {

    @Autowired
    private NewsQueryService newsQueryService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private NewsFixture newsFixture;

    @Autowired
    private LikedNewsFixture likedNewsFixture;

    @Nested
    @DisplayName("전체 소식지 조회")
    class 전체_소식지를_조회한다 {

        @Test
        void 로그인하지_않은_사용자가_전체_소식지를_성공적으로_조회한다() {
            // given
            SiteUser author1 = siteUserFixture.멘토(1, "author1");
            SiteUser author2 = siteUserFixture.멘토(2, "author2");

            News news1 = newsFixture.소식지(author1.getId());
            News news2 = newsFixture.소식지(author1.getId());
            News news3 = newsFixture.소식지(author2.getId());

            // when
            NewsListResponse response = newsQueryService.findNews(null, null);

            // then
            assertAll(
                    () -> assertThat(response.newsResponseList())
                            .extracting(NewsResponse::id)
                            .containsExactlyInAnyOrder(news1.getId(), news2.getId(), news3.getId()),
                    () -> assertThat(response.newsResponseList())
                            .extracting(NewsResponse::updatedAt)
                            .isSortedAccordingTo(Comparator.reverseOrder()),
                    () -> assertThat(response.newsResponseList())
                            .extracting(NewsResponse::isLiked)
                            .containsOnly((Boolean) null)
            );
        }

        @Test
        void 로그인한_사용자가_전체_소식지를_좋아요_상태와_함께_조회한다() {
            // given
            SiteUser author1 = siteUserFixture.멘토(1, "author1");
            SiteUser author2 = siteUserFixture.멘토(2, "author2");
            SiteUser loginUser = siteUserFixture.사용자(3, "loginUser");

            News news1 = newsFixture.소식지(author1.getId());
            News news2 = newsFixture.소식지(author1.getId());
            News news3 = newsFixture.소식지(author2.getId());

            likedNewsFixture.소식지_좋아요(news1.getId(), loginUser.getId());
            likedNewsFixture.소식지_좋아요(news3.getId(), loginUser.getId());

            // when
            NewsListResponse response = newsQueryService.findNews(loginUser.getId(), null);

            // then
            assertAll(
                    () -> assertThat(response.newsResponseList())
                            .extracting(NewsResponse::id)
                            .containsExactlyInAnyOrder(news1.getId(), news2.getId(), news3.getId()),
                    () -> assertThat(response.newsResponseList())
                            .extracting(NewsResponse::updatedAt)
                            .isSortedAccordingTo(Comparator.reverseOrder()),
                    () -> {
                        Map<Long, Boolean> likeStatusMap = response.newsResponseList().stream()
                                .collect(Collectors.toMap(NewsResponse::id, NewsResponse::isLiked));
                        assertThat(likeStatusMap.get(news1.getId())).isTrue();
                        assertThat(likeStatusMap.get(news2.getId())).isFalse();
                        assertThat(likeStatusMap.get(news3.getId())).isTrue();
                    }
            );
        }

        @Test
        void 소식지가_없으면_빈_목록을_반환한다() {
            // when
            NewsListResponse response = newsQueryService.findNews(null, null);

            // then
            assertThat(response.newsResponseList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("특정 사용자의 소식지 조회")
    class 특정_사용자의_소식지를_조회한다 {

        @Test
        void 로그인하지_않은_사용자가_특정_사용자의_소식지_목록을_성공적으로_조회한다() {
            // given
            SiteUser author = siteUserFixture.멘토(1, "author");
            SiteUser otherUser = siteUserFixture.멘토(2, "other");

            News news1 = newsFixture.소식지(author.getId());
            News news2 = newsFixture.소식지(author.getId());
            newsFixture.소식지(otherUser.getId());

            // when
            NewsListResponse response = newsQueryService.findNews(null, author.getId());

            // then
            assertAll(
                    () -> assertThat(response.newsResponseList())
                            .extracting(NewsResponse::id)
                            .containsExactlyInAnyOrder(news1.getId(), news2.getId()),
                    () -> assertThat(response.newsResponseList())
                            .extracting(NewsResponse::updatedAt)
                            .isSortedAccordingTo(Comparator.reverseOrder()),
                    () -> assertThat(response.newsResponseList())
                            .extracting(NewsResponse::isLiked)
                            .containsOnly((Boolean) null)
            );
        }

        @Test
        void 로그인한_사용자가_특정_사용자의_소식지_목록을_성공적으로_조회한다() {
            // given
            SiteUser author = siteUserFixture.멘토(1, "author");
            SiteUser loginUser = siteUserFixture.멘토(2, "loginUser");

            News news1 = newsFixture.소식지(author.getId());
            News news2 = newsFixture.소식지(author.getId());
            News news3 = newsFixture.소식지(author.getId());

            likedNewsFixture.소식지_좋아요(news1.getId(), loginUser.getId());
            likedNewsFixture.소식지_좋아요(news3.getId(), loginUser.getId());

            // when
            NewsListResponse response = newsQueryService.findNews(loginUser.getId(), author.getId());

            // then
            assertAll(
                    () -> assertThat(response.newsResponseList())
                            .extracting(NewsResponse::id)
                            .containsExactlyInAnyOrder(news1.getId(), news2.getId(), news3.getId()),
                    () -> assertThat(response.newsResponseList())
                            .extracting(NewsResponse::updatedAt)
                            .isSortedAccordingTo(Comparator.reverseOrder()),
                    () -> {
                        Map<Long, Boolean> likeStatusMap = response.newsResponseList().stream()
                                .collect(Collectors.toMap(NewsResponse::id, NewsResponse::isLiked));
                        assertThat(likeStatusMap.get(news1.getId())).isTrue();
                        assertThat(likeStatusMap.get(news2.getId())).isFalse();
                        assertThat(likeStatusMap.get(news3.getId())).isTrue();
                    }
            );
        }
    }
}
