package com.example.solidconnection.post.dto;

import com.example.solidconnection.post.domain.Post;

public record PostUpdateResponse(
        Long id
) {
    public static PostUpdateResponse from(Post post) {
        return new PostUpdateResponse(
                post.getId()
        );
    }
}
