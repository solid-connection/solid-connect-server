package com.example.solidconnection.post.dto;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.PostCategory;

public record PostCreateRequest(
        String postCategory,
        String title,
        String content,
        Boolean isQuestion
) {

    public Post toEntity(SiteUser siteUser, Board board) {
        Post post = new Post(
                this.title,
                this.content,
                this.isQuestion,
                0L,
                0L,
                PostCategory.valueOf(this.postCategory)
        );
        post.setBoardAndSiteUser(board, siteUser);
        return post;
    }
}
