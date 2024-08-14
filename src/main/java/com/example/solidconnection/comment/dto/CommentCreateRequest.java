package com.example.solidconnection.comment.dto;

import com.example.solidconnection.comment.domain.Comment;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommentCreateRequest {

    @NotBlank(message = "댓글 내용은 빈 값일 수 없습니다.")
    @Size(min = 1, max = 255, message = "댓글 내용은 최소 1자 이상, 최대 255자 이하여야 합니다.")
    String content;

    @Nullable
    Long parentId;

    public CommentCreateRequest(String content, @Nullable Long parentId) {
        this.content = content;
        this.parentId = parentId;
    }

    public Comment toEntity(SiteUser siteUser, Post post, Comment parentComment) {

        Comment comment = new Comment(
                this.content
        );

        if (parentComment == null) {
            comment.setPostAndSiteUser(post, siteUser);
        } else {
            comment.setParentCommentAndPostAndSiteUser(parentComment, post, siteUser);
        }
        return comment;
    }
}
