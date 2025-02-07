package com.example.solidconnection.board.service;

import com.example.solidconnection.community.domain.post.Post;
import com.example.solidconnection.community.dto.post.BoardFindPostResponse;
import com.example.solidconnection.community.service.board.BoardService;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.BoardCode;
import com.example.solidconnection.type.PostCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("게시판 서비스 테스트")
class BoardServiceTest extends BaseIntegrationTest {

    @Autowired
    private BoardService boardService;

    @Test
    void 게시판_코드와_카테고리로_게시글_목록을_조회한다() {
        // given
        List<Post> posts = List.of(
                미주권_자유게시글, 아시아권_자유게시글, 유럽권_자유게시글, 자유게시판_자유게시글,
                미주권_질문게시글, 아시아권_질문게시글, 유럽권_질문게시글, 자유게시판_질문게시글
        );
        List<Post> expectedPosts = posts.stream()
                .filter(post -> post.getCategory().equals(PostCategory.자유) && post.getBoard().getCode().equals(BoardCode.FREE.name()))
                .toList();
        List<BoardFindPostResponse> expectedResponses = BoardFindPostResponse.from(expectedPosts);

        // when
        List<BoardFindPostResponse> actualResponses = boardService.findPostsByCodeAndPostCategory(
                BoardCode.FREE.name(),
                PostCategory.자유.name()
        );

        // then
        assertThat(actualResponses)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(expectedResponses);
    }

    @Test
    void 전체_카테고리로_조회시_해당_게시판의_모든_게시글을_조회한다() {
        // given
        List<Post> posts = List.of(
                미주권_자유게시글, 아시아권_자유게시글, 유럽권_자유게시글, 자유게시판_자유게시글,
                미주권_질문게시글, 아시아권_질문게시글, 유럽권_질문게시글, 자유게시판_질문게시글
        );
        List<Post> expectedPosts = posts.stream()
                .filter(post -> post.getBoard().getCode().equals(BoardCode.FREE.name()))
                .toList();
        List<BoardFindPostResponse> expectedResponses = BoardFindPostResponse.from(expectedPosts);

        // when
        List<BoardFindPostResponse> actualResponses = boardService.findPostsByCodeAndPostCategory(
                BoardCode.FREE.name(),
                PostCategory.전체.name()
        );

        // then
        assertThat(actualResponses)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(expectedResponses);
    }
}
