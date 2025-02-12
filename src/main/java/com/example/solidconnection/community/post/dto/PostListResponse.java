package com.example.solidconnection.community.post.dto;

import com.example.solidconnection.community.post.domain.PostImage;
import com.example.solidconnection.community.post.domain.Post;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record PostListResponse(
        Long id,
        String title,
        String content,
        Long likeCount,
        Integer commentCount,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        String postCategory,
        String url
) {

    public static PostListResponse from(Post post) {
        return new PostListResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getLikeCount(),
                getCommentCount(post),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                String.valueOf(post.getCategory()),
                getFirstImageUrl(post)
        );
    }

    public static List<PostListResponse> from(List<Post> postList) {
        return postList.stream()
                .map(PostListResponse::from)
                .collect(Collectors.toList());
    }

    private static int getCommentCount(Post post) {
        return post.getCommentList().size();
    }

    private static String getFirstImageUrl(Post post) {
        return post.getPostImageList().stream()
                .findFirst()
                .map(PostImage::getUrl)
                .orElse(null);
    }
}
