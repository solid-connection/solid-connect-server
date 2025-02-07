package com.example.solidconnection.community.dto.post;

import com.example.solidconnection.community.domain.post.Post;

public record PostCreateResponse(
        Long id
) {

    public static PostCreateResponse from(Post post) {
        return new PostCreateResponse(
                post.getId()
        );
    }
}
