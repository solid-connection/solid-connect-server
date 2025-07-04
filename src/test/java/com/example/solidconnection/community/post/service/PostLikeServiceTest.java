package com.example.solidconnection.community.post.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.community.board.fixture.BoardFixture;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.community.post.dto.PostDislikeResponse;
import com.example.solidconnection.community.post.dto.PostLikeResponse;
import com.example.solidconnection.community.post.fixture.PostFixture;
import com.example.solidconnection.community.post.repository.PostLikeRepository;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.solidconnection.common.exception.ErrorCode.DUPLICATE_POST_LIKE;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_POST_LIKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
@DisplayName("게시글 좋아요 서비스 테스트")
class PostLikeServiceTest {

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private BoardFixture boardFixture;

    @Autowired
    private PostFixture postFixture;

    private SiteUser user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
        post = postFixture.게시글(
                "제목1",
                "내용1",
                false,
                PostCategory.자유,
                boardFixture.자유게시판(),
                user
        );
    }

    @Nested
    class 게시글_좋아요_테스트 {

        @Test
        void 게시글을_성공적으로_좋아요한다() {
            // given
            long beforeLikeCount = post.getLikeCount();

            // when
            PostLikeResponse response = postLikeService.likePost(user, post.getId());

            // then
            Post likedPost = postRepository.findById(post.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(response.likeCount()).isEqualTo(beforeLikeCount + 1),
                    () -> assertThat(response.isLiked()).isTrue(),
                    () -> assertThat(likedPost.getLikeCount()).isEqualTo(beforeLikeCount + 1),
                    () -> assertThat(postLikeRepository.findPostLikeByPostAndSiteUser(likedPost, user)).isPresent()
            );
        }

        @Test
        void 이미_좋아요한_게시글을_다시_좋아요하면_예외_응답을_반환한다() {
            // given
            postLikeService.likePost(user, post.getId());

            // when & then
            assertThatThrownBy(() ->
                    postLikeService.likePost(
                            user,
                            post.getId()
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DUPLICATE_POST_LIKE.getMessage());
        }
    }

    @Nested
    class 게시글_좋아요_취소_테스트 {

        @Test
        void 게시글_좋아요를_성공적으로_취소한다() {
            // given
            PostLikeResponse beforeResponse = postLikeService.likePost(user, post.getId());
            long beforeLikeCount = beforeResponse.likeCount();

            // when
            PostDislikeResponse response = postLikeService.dislikePost(user, post.getId());

            // then
            Post unlikedPost = postRepository.findById(post.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(response.likeCount()).isEqualTo(beforeLikeCount - 1),
                    () -> assertThat(response.isLiked()).isFalse(),
                    () -> assertThat(unlikedPost.getLikeCount()).isEqualTo(beforeLikeCount - 1),
                    () -> assertThat(postLikeRepository.findPostLikeByPostAndSiteUser(unlikedPost, user)).isEmpty()
            );
        }

        @Test
        void 좋아요하지_않은_게시글을_좋아요_취소하면_예외_응답을_반환한다() {
            // when & then
            assertThatThrownBy(() ->
                    postLikeService.dislikePost(
                            user,
                            post.getId()
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_LIKE.getMessage());
        }
    }
}
