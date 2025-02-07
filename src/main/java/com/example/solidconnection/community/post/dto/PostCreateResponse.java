package com.example.solidconnection.community.post.dto;

import com.example.solidconnection.community.post.domain.Post;

public record PostCreateResponse(
        Long id
) {

    public static PostCreateResponse from(Post post) {
        return new PostCreateResponse(
                post.getId()
        );
    }
}
