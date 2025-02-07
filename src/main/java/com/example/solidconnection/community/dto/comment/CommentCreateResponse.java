package com.example.solidconnection.community.dto.comment;

import com.example.solidconnection.community.domain.comment.Comment;

public record CommentCreateResponse(
        Long id
) {

    public static CommentCreateResponse from(Comment comment) {
        return new CommentCreateResponse(
                comment.getId()
        );
    }
}
