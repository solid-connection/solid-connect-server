package com.example.solidconnection.community.comment.dto;

import com.example.solidconnection.community.comment.domain.Comment;

public record CommentUpdateResponse(
        Long id
) {

    public static CommentUpdateResponse from(Comment comment) {
        return new CommentUpdateResponse(
                comment.getId()
        );
    }
}
