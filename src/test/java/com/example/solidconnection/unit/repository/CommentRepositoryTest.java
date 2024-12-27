package com.example.solidconnection.unit.repository;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.comment.domain.Comment;
import com.example.solidconnection.comment.repository.CommentRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.post.domain.Post;
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

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_COMMENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestContainerDataJpaTest
@DisplayName("댓글 레포지토리 테스트")
class CommentRepositoryTest {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private SiteUserRepository siteUserRepository;
    @Autowired
    private CommentRepository commentRepository;

    private Board board;
    private SiteUser siteUser;
    private Post post;
    private Comment parentComment;
    private Comment childComment;

    @BeforeEach
    public void setUp() {
        board = createBoard();
        boardRepository.save(board);

        siteUser = createSiteUser();
        siteUserRepository.save(siteUser);

        post = createPost(board, siteUser);
        post = postRepository.save(post);

        parentComment = createParentComment();
        childComment = createChildComment();
        commentRepository.save(parentComment);
        commentRepository.save(childComment);
    }

    private Board createBoard() {
        return new Board(
                "FREE", "자유게시판");
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

    private Comment createParentComment() {
        Comment comment = new Comment(
                "parent"
        );
        comment.setPostAndSiteUser(post, siteUser);
        return comment;
    }

    private Comment createChildComment() {
        Comment comment = new Comment(
                "child"
        );
        comment.setParentCommentAndPostAndSiteUser(parentComment, post, siteUser);
        return comment;
    }

    @Test
    @Transactional
    public void 재귀쿼리로_댓글트리를_조회한다() {
        // when
        List<Comment> commentTreeByPostId = commentRepository.findCommentTreeByPostId(post.getId());

        // then
        List<Comment> expectedResponse = List.of(parentComment, childComment);
        assertEquals(commentTreeByPostId, expectedResponse);
    }

    @Test
    @Transactional
    public void 댓글을_조회한다() {
        // when
        Comment foundComment = commentRepository.getById(parentComment.getId());

        // then
        assertEquals(parentComment, foundComment);
    }

    @Test
    @Transactional
    public void 댓글을_조회할_때_유효한_댓글이_아니라면_예외_응답을_반환한다() {
        // given
        Long invalidId = -1L;

        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> {
            commentRepository.getById(invalidId);
        });
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_COMMENT_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_COMMENT_ID.getCode());
    }
}
