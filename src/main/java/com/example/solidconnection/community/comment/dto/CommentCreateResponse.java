package com.example.solidconnection.community.comment.dto;

import com.example.solidconnection.community.comment.domain.Comment;

public record CommentCreateResponse(
        Long id
) {

    public static CommentCreateResponse from(Comment comment) {
        return new CommentCreateResponse(
                comment.getId()
        );
    }
}
