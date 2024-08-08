package com.example.solidconnection.post.dto;

import com.example.solidconnection.post.domain.Post;

public record PostCreateResponse(
        Long id
) {

    public static PostCreateResponse from(Post post) {
        return new PostCreateResponse(
                post.getId()
        );
    }
}
