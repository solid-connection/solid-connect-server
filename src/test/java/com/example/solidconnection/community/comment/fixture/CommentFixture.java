package com.example.solidconnection.community.comment.fixture;

import com.example.solidconnection.community.comment.domain.Comment;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class CommentFixture {

    private final CommentFixtureBuilder commentFixtureBuilder;

    public Comment 부모_댓글(String content, Post post, SiteUser siteUser) {
        return commentFixtureBuilder
                .content(content)
                .post(post)
                .siteUser(siteUser)
                .createParent();
    }

    public Comment 자식_댓글(
            String content,
            Post post,
            SiteUser siteUser,
            Comment parentComment) {
        return commentFixtureBuilder
                .content(content)
                .post(post)
                .siteUser(siteUser)
                .parentComment(parentComment)
                .createChild();
    }
}
