package com.example.solidconnection.post.controller;

import com.example.solidconnection.post.dto.*;
import com.example.solidconnection.post.service.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static com.example.solidconnection.config.swagger.SwaggerConfig.ACCESS_TOKEN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/communities")
@SecurityRequirements
@SecurityRequirement(name = ACCESS_TOKEN)
public class PostController {

    private final PostService postService;

    @PostMapping(value = "/{code}/posts")
    public ResponseEntity<?> createPost(
            Principal principal,
            @PathVariable("code") String code,
            @Valid @RequestPart("postCreateRequest") PostCreateRequest postCreateRequest,
            @RequestParam(value = "file", required = false) List<MultipartFile> imageFile) {

        if (imageFile == null) {
            imageFile = Collections.emptyList();
        }
        PostCreateResponse post = postService
                .createPost(principal.getName(), code, postCreateRequest, imageFile);
        return ResponseEntity.ok().body(post);
    }

    @PatchMapping(value = "/{code}/posts/{post_id}")
    public ResponseEntity<?> updatePost(
            Principal principal,
            @PathVariable("code") String code,
            @PathVariable("post_id") Long postId,
            @Valid @RequestPart("postUpdateRequest") PostUpdateRequest postUpdateRequest,
            @RequestParam(value = "file", required = false) List<MultipartFile> imageFile) {

        if (imageFile == null) {
            imageFile = Collections.emptyList();
        }
        PostUpdateResponse postUpdateResponse = postService
                .updatePost(principal.getName(), code, postId, postUpdateRequest, imageFile);
        return ResponseEntity.ok().body(postUpdateResponse);
    }


    @GetMapping("/{code}/posts/{post_id}")
    public ResponseEntity<?> findPostById(
            Principal principal,
            @PathVariable("code") String code,
            @PathVariable("post_id") Long postId) {

        PostFindResponse postFindResponse = postService
                .findPostById(principal.getName(), code, postId);
        return ResponseEntity.ok().body(postFindResponse);
    }

    @DeleteMapping(value = "/{code}/posts/{post_id}")
    public ResponseEntity<?> deletePostById(
            Principal principal,
            @PathVariable("code") String code,
            @PathVariable("post_id") Long postId) {

        PostDeleteResponse postDeleteResponse = postService.deletePostById(principal.getName(), code, postId);
        return ResponseEntity.ok().body(postDeleteResponse);
    }

    @PostMapping(value = "/{code}/posts/{post_id}/like")
    public ResponseEntity<?> likePost(
            Principal principal,
            @PathVariable("code") String code,
            @PathVariable("post_id") Long postId
    ) {

        PostLikeResponse postLikeResponse = postService.likePost(principal.getName(), code, postId);
        return ResponseEntity.ok().body(postLikeResponse);
    }

    @DeleteMapping(value = "/{code}/posts/{post_id}/like")
    public ResponseEntity<?> dislikePost(
            Principal principal,
            @PathVariable("code") String code,
            @PathVariable("post_id") Long postId
    ) {

        PostDislikeResponse postDislikeResponse = postService.dislikePost(principal.getName(), code, postId);
        return ResponseEntity.ok().body(postDislikeResponse);
    }
}
