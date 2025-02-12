package com.example.solidconnection.community.post.dto;

import com.example.solidconnection.community.post.domain.Post;

public record PostUpdateResponse(
        Long id
) {
    public static PostUpdateResponse from(Post post) {
        return new PostUpdateResponse(
                post.getId()
        );
    }
}
