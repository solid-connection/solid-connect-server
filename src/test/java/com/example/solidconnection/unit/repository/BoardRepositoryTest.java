package com.example.solidconnection.unit.repository;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_BOARD_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("게시판 레포지토리 테스트")
public class BoardRepositoryTest {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private SiteUserRepository siteUserRepository;
    @Autowired
    private EntityManager entityManager;

    private Board board;
    private SiteUser siteUser;
    private Post post;

    @BeforeEach
    public void setUp() {
        board = createBoard();
        boardRepository.save(board);

        siteUser = createSiteUser();
        siteUserRepository.save(siteUser);

        post = createPost(board, siteUser);
        post = postRepository.save(post);

        entityManager.flush();
        entityManager.clear();
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

    @Test
    @Transactional
    public void 게시판을_조회할_때_게시글은_즉시_로딩한다() {
        // when
        Board foundBoard = boardRepository.getByCodeUsingEntityGraph(board.getCode());
        foundBoard.getPostList().size(); // 추가쿼리 발생하지 않는다.

        // then
        assertThat(foundBoard.getCode()).isEqualTo(board.getCode());
    }

    @Test
    @Transactional
    public void 게시판을_조회할_때_게시글은_즉시_로딩한다_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // given
        String invalidCode = "INVALID_CODE";

        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> {
            boardRepository.getByCodeUsingEntityGraph(invalidCode);
        });
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_BOARD_CODE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_BOARD_CODE.getCode());
    }

    @Test
    @Transactional
    public void 게시판을_조회한다() {
        // when
        Board foundBoard = boardRepository.getByCode(board.getCode());

        // then
        assertEquals(board.getCode(), foundBoard.getCode());
    }

    @Test
    @Transactional
    public void 게시판을_조회할_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // given
        String invalidCode = "INVALID_CODE";

        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> {
            boardRepository.getByCode(invalidCode);
        });
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_BOARD_CODE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_BOARD_CODE.getCode());
    }
}
