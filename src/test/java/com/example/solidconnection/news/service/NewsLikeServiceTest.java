package com.example.solidconnection.news.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.LikedNewsResponse;
import com.example.solidconnection.news.fixture.NewsFixture;
import com.example.solidconnection.news.repository.LikedNewsRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_LIKED_NEWS;
import static com.example.solidconnection.common.exception.ErrorCode.NOT_LIKED_NEWS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@TestContainerSpringBootTest
@DisplayName("소식지 좋아요 서비스 테스트")
class NewsLikeServiceTest {

    @Autowired
    private NewsLikeService newsLikeService;

    @Autowired
    private LikedNewsRepository likedNewsRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private NewsFixture newsFixture;

    private SiteUser user;
    private News news;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
        news = newsFixture.소식지(siteUserFixture.멘토(1, "mentor").getId());
    }

    @Nested
    class 소식지_좋아요_상태를_조회한다 {

        @Test
        void 좋아요한_소식지인지_확인한다() {
            // given
            newsLikeService.addNewsLike(user.getId(), news.getId());

            // when
            LikedNewsResponse response = newsLikeService.getNewsLikeStatus(user.getId(), news.getId());

            // then
            assertThat(response.isLike()).isTrue();
        }

        @Test
        void 좋아요하지_않은_소식지의_좋아요_상태를_조회한다() {
            // when
            LikedNewsResponse response = newsLikeService.getNewsLikeStatus(user.getId(), news.getId());

            // then
            assertThat(response.isLike()).isFalse();
        }
    }

    @Nested
    class 소식지_좋아요를_등록한다 {

        @Test
        void 성공적으로_좋아요를_등록한다() {
            // when
            newsLikeService.addNewsLike(user.getId(), news.getId());

            // then
            assertThat(likedNewsRepository.existsByNewsIdAndSiteUserId(news.getId(), user.getId())).isTrue();
        }

        @Test
        void 이미_좋아요했으면_예외_응답을_반환한다() {
            // given
            newsLikeService.addNewsLike(user.getId(), news.getId());

            // when & then
            assertThatCode(() -> newsLikeService.addNewsLike(user.getId(), news.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ALREADY_LIKED_NEWS.getMessage());
        }
    }

    @Nested
    class 소식지_좋아요를_취소한다 {

        @Test
        void 성공적으로_좋아요를_취소한다() {
            // given
            newsLikeService.addNewsLike(user.getId(), news.getId());

            // when
            newsLikeService.cancelNewsLike(user.getId(), news.getId());

            // then
            assertThat(likedNewsRepository.existsByNewsIdAndSiteUserId(news.getId(), user.getId())).isFalse();
        }

        @Test
        void 좋아요하지_않았으면_예외_응답을_반환한다() {
            // when & then
            assertThatCode(() -> newsLikeService.cancelNewsLike(user.getId(), news.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_LIKED_NEWS.getMessage());
        }
    }
}
