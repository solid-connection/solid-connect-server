package com.example.solidconnection.community.post.service;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.dto.PostDislikeResponse;
import com.example.solidconnection.community.post.dto.PostLikeResponse;
import com.example.solidconnection.community.post.repository.PostLikeRepository;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.PostCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.solidconnection.custom.exception.ErrorCode.DUPLICATE_POST_LIKE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_LIKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("게시글 좋아요 서비스 테스트")
class PostLikeServiceTest extends BaseIntegrationTest {

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Nested
    class 게시글_좋아요_테스트 {

        @Test
        void 게시글을_성공적으로_좋아요한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            long beforeLikeCount = testPost.getLikeCount();

            // when
            PostLikeResponse response = postLikeService.likePost(
                    테스트유저_1,
                    testPost.getId()
            );

            // then
            Post likedPost = postRepository.findById(testPost.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(response.likeCount()).isEqualTo(beforeLikeCount + 1),
                    () -> assertThat(response.isLiked()).isTrue(),
                    () -> assertThat(likedPost.getLikeCount()).isEqualTo(beforeLikeCount + 1),
                    () -> assertThat(postLikeRepository.findPostLikeByPostAndSiteUser(likedPost, 테스트유저_1)).isPresent()
            );
        }

        @Test
        void 이미_좋아요한_게시글을_다시_좋아요하면_예외_응답을_반환한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            postLikeService.likePost(테스트유저_1,  testPost.getId());

            // when & then
            assertThatThrownBy(() ->
                    postLikeService.likePost(
                            테스트유저_1,
                            testPost.getId()
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
            Post testPost = createPost(자유게시판, 테스트유저_1);
            PostLikeResponse beforeResponse = postLikeService.likePost(테스트유저_1,  testPost.getId());
            long beforeLikeCount = beforeResponse.likeCount();

            // when
            PostDislikeResponse response = postLikeService.dislikePost(
                    테스트유저_1,
                    testPost.getId()
            );

            // then
            Post unlikedPost = postRepository.findById(testPost.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(response.likeCount()).isEqualTo(beforeLikeCount - 1),
                    () -> assertThat(response.isLiked()).isFalse(),
                    () -> assertThat(unlikedPost.getLikeCount()).isEqualTo(beforeLikeCount - 1),
                    () -> assertThat(postLikeRepository.findPostLikeByPostAndSiteUser(unlikedPost, 테스트유저_1)).isEmpty()
            );
        }

        @Test
        void 좋아요하지_않은_게시글을_좋아요_취소하면_예외_응답을_반환한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);

            // when & then
            assertThatThrownBy(() ->
                    postLikeService.dislikePost(
                            테스트유저_1,
                            testPost.getId()
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_LIKE.getMessage());
        }
    }

    private Post createPost(Board board, SiteUser siteUser) {
        Post post = new Post(
                "테스트 제목",
                "테스트 내용",
                false,
                0L,
                0L,
                PostCategory.자유
        );
        post.setBoardAndSiteUser(board, siteUser);
        return postRepository.save(post);
    }
}
