package com.example.solidconnection.community.post.dto;

import com.example.solidconnection.community.board.dto.PostFindBoardResponse;
import com.example.solidconnection.community.comment.dto.PostFindCommentResponse;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.siteuser.dto.PostFindSiteUserResponse;

import java.time.ZonedDateTime;
import java.util.List;

public record PostFindResponse(
        Long id,
        String title,
        String content,
        Boolean isQuestion,
        Long likeCount,
        Long viewCount,
        Integer commentCount,
        String postCategory,
        Boolean isOwner,
        Boolean isLiked,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        PostFindBoardResponse postFindBoardResponse,
        PostFindSiteUserResponse postFindSiteUserResponse,
        List<PostFindCommentResponse> postFindCommentResponses,
        List<PostFindPostImageResponse> postFindPostImageResponses
) {

    public static PostFindResponse from(Post post, Boolean isOwner, Boolean isLiked, PostFindBoardResponse postFindBoardResponse,
                                        PostFindSiteUserResponse postFindSiteUserResponse,
                                        List<PostFindCommentResponse> postFindCommentResponses,
                                        List<PostFindPostImageResponse> postFindPostImageResponses
    ) {
        return new PostFindResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getIsQuestion(),
                post.getLikeCount(),
                post.getViewCount(),
                postFindCommentResponses.size(),
                String.valueOf(post.getCategory()),
                isOwner,
                isLiked,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                postFindBoardResponse,
                postFindSiteUserResponse,
                postFindCommentResponses,
                postFindPostImageResponses
        );
    }
}
