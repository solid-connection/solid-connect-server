package com.example.solidconnection.community.post.dto;

import com.example.solidconnection.community.post.domain.PostImage;

import java.util.List;
import java.util.stream.Collectors;

public record PostFindPostImageResponse(
        Long id,
        String url
) {
    public static PostFindPostImageResponse from(PostImage postImage) {
        return new PostFindPostImageResponse(
                postImage.getId(),
                postImage.getUrl()
        );
    }

    public static List<PostFindPostImageResponse> from(List<PostImage> postImageList) {
        return postImageList.stream()
                .map(PostFindPostImageResponse::from)
                .collect(Collectors.toList());
    }
}
