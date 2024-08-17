package com.example.solidconnection.post.dto;

import com.example.solidconnection.post.domain.Post;

public record PostDislikeResponse(
        Long likeCount,
        Boolean isLiked
) {
    public static PostDislikeResponse from(Post post) {
        return new PostDislikeResponse(
                post.getLikeCount(),
                false
        );
    }
}
