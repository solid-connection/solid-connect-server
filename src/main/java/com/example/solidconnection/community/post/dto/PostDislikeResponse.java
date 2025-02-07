package com.example.solidconnection.community.post.dto;

import com.example.solidconnection.community.post.domain.Post;

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
