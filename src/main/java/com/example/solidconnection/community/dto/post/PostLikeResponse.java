package com.example.solidconnection.community.dto.post;

import com.example.solidconnection.community.domain.post.Post;

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
