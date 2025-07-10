package com.example.solidconnection.community.comment.fixture;

import com.example.solidconnection.community.comment.domain.Comment;
import com.example.solidconnection.community.comment.repository.CommentRepository;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class CommentFixtureBuilder {

    private final CommentRepository commentRepository;

    private String content;
    private Post post;
    private SiteUser siteUser;
    private Comment parentComment;

    public CommentFixtureBuilder content(String content) {
        this.content = content;
        return this;
    }

    public CommentFixtureBuilder post(Post post) {
        this.post = post;
        return this;
    }

    public CommentFixtureBuilder siteUser(SiteUser siteUser) {
        this.siteUser = siteUser;
        return this;
    }

    public CommentFixtureBuilder parentComment(Comment parentComment) {
        this.parentComment = parentComment;
        return this;
    }

    public Comment createParent() {
        Comment comment = new Comment(content);
        comment.setPostAndSiteUserId(post, siteUser.getId());
        return commentRepository.save(comment);
    }

    public Comment createChild() {
        Comment comment = new Comment(content);
        comment.setPostAndSiteUserId(post, siteUser.getId());
        comment.setParentCommentAndPostAndSiteUserId(parentComment, post, siteUser.getId());
        return commentRepository.save(comment);
    }
}
