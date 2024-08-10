package com.example.solidconnection.post.dto;

import com.example.solidconnection.entity.PostImage;
import com.example.solidconnection.post.domain.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record BoardFindPostResponse(
        Long id,
        String title,
        String content,
        Long likeCount,
        Integer commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String postCategory,
        String url
) {

    public static BoardFindPostResponse from(Post post) {
        return new BoardFindPostResponse(
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

    private static int getCommentCount(Post post) {
        return post.getCommentList().size();
    }

    private static String getFirstImageUrl(Post post) {
        return post.getPostImageList().stream()
                .findFirst()
                .map(PostImage::getUrl)
                .orElse(null);
    }

    public static List<BoardFindPostResponse> from(List<Post> postList) {
        return postList.stream()
                .map(BoardFindPostResponse::from)
                .collect(Collectors.toList());
    }
}
