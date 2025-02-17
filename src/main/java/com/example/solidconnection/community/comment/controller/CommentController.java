package com.example.solidconnection.community.comment.controller;

import com.example.solidconnection.community.comment.dto.CommentCreateRequest;
import com.example.solidconnection.community.comment.dto.CommentCreateResponse;
import com.example.solidconnection.community.comment.dto.CommentDeleteResponse;
import com.example.solidconnection.community.comment.dto.CommentUpdateRequest;
import com.example.solidconnection.community.comment.dto.CommentUpdateResponse;
import com.example.solidconnection.community.comment.service.CommentService;
import com.example.solidconnection.custom.resolver.AuthorizedUser;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> createComment(
            @AuthorizedUser SiteUser siteUser,
            @Valid @RequestBody CommentCreateRequest commentCreateRequest
    ) {
        CommentCreateResponse response = commentService.createComment(siteUser, commentCreateRequest);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/{comment_id}")
    public ResponseEntity<?> updateComment(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("comment_id") Long commentId,
            @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    ) {
        CommentUpdateResponse response = commentService.updateComment(siteUser, commentId, commentUpdateRequest);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{comment_id}")
    public ResponseEntity<?> deleteCommentById(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("comment_id") Long commentId
    ) {
        CommentDeleteResponse response = commentService.deleteCommentById(siteUser, commentId);
        return ResponseEntity.ok().body(response);
    }
}
