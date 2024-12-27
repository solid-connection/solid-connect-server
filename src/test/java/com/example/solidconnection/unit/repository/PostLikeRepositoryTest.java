package com.example.solidconnection.unit.repository;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.domain.PostLike;
import com.example.solidconnection.post.repository.PostLikeRepository;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerDataJpaTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_LIKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestContainerDataJpaTest
@DisplayName("게시글 좋아요 레포지토리 테스트")
class PostLikeRepositoryTest {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private SiteUserRepository siteUserRepository;
    @Autowired
    private PostLikeRepository postLikeRepository;

    private Post post;
    private Board board;
    private SiteUser siteUser;
    private PostLike postLike;


    @BeforeEach
    void setUp() {
        board = createBoard();
        boardRepository.save(board);
        siteUser = createSiteUser();
        siteUserRepository.save(siteUser);
        post = createPost(board, siteUser);
        post = postRepository.save(post);
        postLike = createPostLike(post, siteUser);
        postLikeRepository.save(postLike);
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

    private PostLike createPostLike(Post post, SiteUser siteUser) {
        PostLike postLike = new PostLike();
        postLike.setPostAndSiteUser(post, siteUser);
        return postLike;
    }

    @Test
    @Transactional
    void 게시글_좋아요를_조회한다() {
        // when
        PostLike foundPostLike = postLikeRepository.getByPostAndSiteUser(post, siteUser);

        // then
        assertEquals(foundPostLike, postLike);
    }

    @Test
    @Transactional
    void 게시글_좋아요를_조회할_때_유효한_좋아요가_아니라면_예외_응답을_반환한다() {
        // given
        postLike.resetPostAndSiteUser();
        postLikeRepository.delete(postLike);

        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> {
            postLikeRepository.getByPostAndSiteUser(post, siteUser);
        });
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_LIKE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_LIKE.getCode());
    }
}
