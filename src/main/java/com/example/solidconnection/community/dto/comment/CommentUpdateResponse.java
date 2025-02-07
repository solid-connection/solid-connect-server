package com.example.solidconnection.community.dto.comment;

import com.example.solidconnection.community.domain.comment.Comment;

public record CommentUpdateResponse(
        Long id
) {

    public static CommentUpdateResponse from(Comment comment) {
        return new CommentUpdateResponse(
                comment.getId()
        );
    }
}
