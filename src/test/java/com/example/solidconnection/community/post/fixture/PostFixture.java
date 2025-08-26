package com.example.solidconnection.community.post.fixture;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class PostFixture {

    private final PostFixtureBuilder postFixtureBuilder;

    public Post 게시글(
            Board board,
            SiteUser siteUser
    ) {
        return postFixtureBuilder
                .title("제목")
                .content("내용")
                .isQuestion(false)
                .likeCount(0L)
                .postCategory(PostCategory.자유)
                .board(board)
                .siteUser(siteUser)
                .create();
    }

    public Post 게시글(
            String title,
            String content,
            Boolean isQuestion,
            PostCategory postCategory,
            Board board,
            SiteUser siteUser
    ) {
        return postFixtureBuilder
                .title(title)
                .content(content)
                .isQuestion(isQuestion)
                .likeCount(0L)
                .viewCount(0L)
                .postCategory(postCategory)
                .board(board)
                .siteUser(siteUser)
                .create();
    }
}
