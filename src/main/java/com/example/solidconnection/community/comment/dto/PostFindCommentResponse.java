package com.example.solidconnection.community.comment.dto;

import com.example.solidconnection.community.comment.domain.Comment;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.PostFindSiteUserResponse;

import java.time.ZonedDateTime;

public record PostFindCommentResponse(
        Long id,
        Long parentId,
        String content,
        Boolean isOwner,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        PostFindSiteUserResponse postFindSiteUserResponse
) {

    public static PostFindCommentResponse from(Boolean isOwner, Comment comment, SiteUser siteUser) {
        return new PostFindCommentResponse(
                comment.getId(),
                getParentCommentId(comment),
                getDisplayContent(comment),
                isOwner,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                PostFindSiteUserResponse.from(siteUser)
        );
    }

    private static Long getParentCommentId(Comment comment) {
        if (comment.getParentComment() != null) {
            return comment.getParentComment().getId();
        }
        return null;
    }
    private static String getDisplayContent(Comment comment) {
        return comment.isDeleted() ? "" : comment.getContent();
    }
}
