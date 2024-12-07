package com.example.solidconnection.post.dto;

import com.example.solidconnection.post.domain.Post;

public record PostLikeResponse(
        Long likeCount,
        Boolean isLiked
) {
    public static PostLikeResponse from(Post post) {
        return new PostLikeResponse(
                post.getLikeCount(),
                true
        );
    }
}
