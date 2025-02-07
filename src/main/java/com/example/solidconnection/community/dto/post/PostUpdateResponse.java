package com.example.solidconnection.community.dto.post;

import com.example.solidconnection.community.domain.post.Post;

public record PostUpdateResponse(
        Long id
) {
    public static PostUpdateResponse from(Post post) {
        return new PostUpdateResponse(
                post.getId()
        );
    }
}
