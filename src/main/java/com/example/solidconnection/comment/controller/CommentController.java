package com.example.solidconnection.comment.controller;

import com.example.solidconnection.comment.dto.CommentCreateRequest;
import com.example.solidconnection.comment.dto.CommentCreateResponse;
import com.example.solidconnection.comment.dto.CommentDeleteResponse;
import com.example.solidconnection.comment.dto.CommentUpdateRequest;
import com.example.solidconnection.comment.dto.CommentUpdateResponse;
import com.example.solidconnection.comment.service.CommentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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

import java.security.Principal;

import static com.example.solidconnection.config.swagger.SwaggerConfig.ACCESS_TOKEN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@SecurityRequirements
@SecurityRequirement(name = ACCESS_TOKEN)
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{post_id}/comments")
    public ResponseEntity<?> createComment(
            Principal principal,
            @PathVariable("post_id") Long postId,
            @Valid @RequestBody CommentCreateRequest commentCreateRequest
    ) {
        CommentCreateResponse commentCreateResponse = commentService.createComment(
                principal.getName(), postId, commentCreateRequest);
        return ResponseEntity.ok().body(commentCreateResponse);
    }

    @PatchMapping("/{post_id}/comments/{comment_id}")
    public ResponseEntity<?> updateComment(
            Principal principal,
            @PathVariable("post_id") Long postId,
            @PathVariable("comment_id") Long commentId,
            @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    ) {
        CommentUpdateResponse commentUpdateResponse = commentService.updateComment(
                principal.getName(), postId, commentId, commentUpdateRequest
        );
        return ResponseEntity.ok().body(commentUpdateResponse);
    }

    @DeleteMapping("/{post_id}/comments/{comment_id}")
    public ResponseEntity<?> deleteCommentById(
            Principal principal,
            @PathVariable("post_id") Long postId,
            @PathVariable("comment_id") Long commentId
    ) {
        CommentDeleteResponse commentDeleteResponse = commentService.deleteCommentById(principal.getName(), postId, commentId);
        return ResponseEntity.ok().body(commentDeleteResponse);
    }
}
