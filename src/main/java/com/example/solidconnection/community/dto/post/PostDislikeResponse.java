package com.example.solidconnection.community.dto.post;

import com.example.solidconnection.community.domain.post.Post;

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
