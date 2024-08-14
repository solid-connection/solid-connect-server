package com.example.solidconnection.unit.service;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.board.service.BoardService;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.dto.BoardFindPostResponse;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("게시판 서비스 테스트")
class BoardServiceTest {
    @InjectMocks
    BoardService boardService;
    @Mock
    BoardRepository boardRepository;

    private SiteUser siteUser;
    private Board board;
    private List<Post> postList = new ArrayList<>();
    private List<Post> freePostList = new ArrayList<>();
    private List<Post> questionPostList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        siteUser = createSiteUser();
        board = createBoard("FREE", "자유게시판");

        Post post_question_1 = createPost("질문", board, siteUser);
        Post post_free_1 = createPost("자유", board, siteUser);
        Post post_free_2 = createPost("자유", board, siteUser);

        postList.add(post_question_1);
        postList.add(post_free_1);
        postList.add(post_free_2);
        questionPostList.add(post_question_1);
        freePostList.add(post_free_1);
        freePostList.add(post_free_2);
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

    private Board createBoard(String code, String koreanName) {
        return new Board(code, koreanName);
    }

    private Post createPost(String postCategory, Board board, SiteUser siteUser) {
        Post post = new Post(
                "title",
                "content",
                false,
                0L,
                0L,
                PostCategory.valueOf(postCategory)
        );
        post.setBoardAndSiteUser(board, siteUser);
        return post;
    }

    @Test
    void 게시판을_조회할_때_게시판_코드와_게시글_카테고리에_따라서_조회한다() {
        // Given
        String category = "자유";
        when(boardRepository.getByCodeUsingEntityGraph(board.getCode())).thenReturn(board);

        // When
        List<BoardFindPostResponse> responses = boardService.findPostsByCodeAndPostCategory(board.getCode(), category);

        // Then
        List<BoardFindPostResponse> expectedResponses = freePostList.stream()
                .map(BoardFindPostResponse::from)
                .toList();
        assertIterableEquals(expectedResponses, responses);
        verify(boardRepository, times(1)).getByCodeUsingEntityGraph(board.getCode());
    }

    @Test
    void 게시판을_조회할_때_카테고리가_전체라면_해당_게시판의_모든_게시글을_조회한다() {
        // Given
        String category = "전체";
        when(boardRepository.getByCodeUsingEntityGraph(board.getCode())).thenReturn(board);

        // When
        List<BoardFindPostResponse> responses = boardService.findPostsByCodeAndPostCategory(board.getCode(), category);

        // Then
        List<BoardFindPostResponse> expectedResponses = postList.stream()
                .map(BoardFindPostResponse::from)
                .toList();
        assertIterableEquals(expectedResponses, responses);
        verify(boardRepository, times(1)).getByCodeUsingEntityGraph(board.getCode());
    }

    @Test
    void 게시판을_조회할_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidCode = "INVALID_CODE";
        String category = "자유";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            boardService.findPostsByCodeAndPostCategory(invalidCode, category);
        });
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getCode());
    }

    @Test
    void 게시판을_조회할_때_유효한_카테고리가_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidCategory = "INVALID_CATEGORY";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            boardService.findPostsByCodeAndPostCategory(board.getCode(), invalidCategory);
        });
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_POST_CATEGORY.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_POST_CATEGORY.getCode());
    }
}
