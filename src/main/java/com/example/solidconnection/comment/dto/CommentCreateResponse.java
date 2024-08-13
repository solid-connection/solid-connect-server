package com.example.solidconnection.comment.dto;

import com.example.solidconnection.comment.domain.Comment;

public record CommentCreateResponse(
        Long id
) {

    public static CommentCreateResponse from(Comment comment) {
        return new CommentCreateResponse(
                comment.getId()
        );
    }
}
