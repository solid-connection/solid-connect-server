package com.example.solidconnection.comment.dto;

import com.example.solidconnection.comment.domain.Comment;

public record CommentUpdateResponse(
        Long id
) {

    public static CommentUpdateResponse from(Comment comment) {
        return new CommentUpdateResponse(
                comment.getId()
        );
    }
}
