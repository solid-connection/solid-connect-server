package com.example.solidconnection.post.service;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.dto.PostDislikeResponse;
import com.example.solidconnection.post.dto.PostLikeResponse;
import com.example.solidconnection.post.repository.PostLikeRepository;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.BoardCode;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
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
    private SiteUserRepository siteUserRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Nested
    class 게시글_좋아요_테스트 {

        @Test
        void 게시글을_성공적으로_좋아요한다() {
            // given
            SiteUser testUser = createSiteUser();
            Board testBoard = createBoard(BoardCode.FREE);
            Post testPost = createPost(testBoard, testUser);
            long beforeLikeCount = testPost.getLikeCount();

            // when
            PostLikeResponse response = postLikeService.likePost(
                    testUser.getEmail(),
                    testBoard.getCode(),
                    testPost.getId()
            );
            Post likedPost = postRepository.findById(testPost.getId()).orElseThrow();

            // then
            assertAll(
                    () -> assertThat(response.likeCount()).isEqualTo(beforeLikeCount + 1),
                    () -> assertThat(response.isLiked()).isTrue(),
                    () -> assertThat(likedPost.getLikeCount()).isEqualTo(beforeLikeCount + 1),
                    () -> assertThat(postLikeRepository.findPostLikeByPostAndSiteUser(likedPost, testUser)).isPresent()
            );
        }

        @Test
        void 이미_좋아요한_게시글을_다시_좋아요하면_예외_응답을_반환한다() {
            // given
            SiteUser testUser = createSiteUser();
            Board testBoard = createBoard(BoardCode.FREE);
            Post testPost = createPost(testBoard, testUser);
            postLikeService.likePost(testUser.getEmail(), testBoard.getCode(), testPost.getId());

            // when & then
            assertThatThrownBy(() ->
                    postLikeService.likePost(
                            testUser.getEmail(),
                            testBoard.getCode(),
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
            SiteUser testUser = createSiteUser();
            Board testBoard = createBoard(BoardCode.FREE);
            Post testPost = createPost(testBoard, testUser);
            PostLikeResponse beforeResponse = postLikeService.likePost(testUser.getEmail(), testBoard.getCode(), testPost.getId());
            long beforeLikeCount = beforeResponse.likeCount();

            // when
            PostDislikeResponse response = postLikeService.dislikePost(
                    testUser.getEmail(),
                    testBoard.getCode(),
                    testPost.getId()
            );
            Post unlikedPost = postRepository.findById(testPost.getId()).orElseThrow();

            // then
            assertAll(
                    () -> assertThat(response.likeCount()).isEqualTo(beforeLikeCount - 1),
                    () -> assertThat(response.isLiked()).isFalse(),
                    () -> assertThat(unlikedPost.getLikeCount()).isEqualTo(beforeLikeCount - 1),
                    () -> assertThat(postLikeRepository.findPostLikeByPostAndSiteUser(unlikedPost, testUser)).isEmpty()
            );
        }

        @Test
        void 좋아요하지_않은_게시글을_좋아요_취소하면_예외_응답을_반환한다() {
            // given
            SiteUser testUser = createSiteUser();
            Board testBoard = createBoard(BoardCode.FREE);
            Post testPost = createPost(testBoard, testUser);

            // when & then
            assertThatThrownBy(() ->
                    postLikeService.dislikePost(
                            testUser.getEmail(),
                            testBoard.getCode(),
                            testPost.getId()
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_LIKE.getMessage());
        }
    }

    private SiteUser createSiteUser() {
        SiteUser siteUser = new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
        return siteUserRepository.save(siteUser);
    }

    private Board createBoard(BoardCode boardCode) {
        Board board = new Board(boardCode.name(), "자유게시판");
        return boardRepository.save(board);
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
