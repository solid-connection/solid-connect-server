package com.example.solidconnection.community.post.controller;

import com.example.solidconnection.community.post.dto.PostCreateRequest;
import com.example.solidconnection.community.post.dto.PostCreateResponse;
import com.example.solidconnection.community.post.dto.PostDeleteResponse;
import com.example.solidconnection.community.post.dto.PostDislikeResponse;
import com.example.solidconnection.community.post.dto.PostFindResponse;
import com.example.solidconnection.community.post.dto.PostLikeResponse;
import com.example.solidconnection.community.post.dto.PostUpdateRequest;
import com.example.solidconnection.community.post.dto.PostUpdateResponse;
import com.example.solidconnection.community.post.service.PostCommandService;
import com.example.solidconnection.community.post.service.PostLikeService;
import com.example.solidconnection.community.post.service.PostQueryService;
import com.example.solidconnection.custom.resolver.AuthorizedUser;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;
    private final PostLikeService postLikeService;

    @PostMapping
    public ResponseEntity<?> createPost(
            @AuthorizedUser SiteUser siteUser,
            @Valid @RequestPart("postCreateRequest") PostCreateRequest postCreateRequest,
            @RequestParam(value = "file", required = false) List<MultipartFile> imageFile
    ) {
        if (imageFile == null) {
            imageFile = Collections.emptyList();
        }
        PostCreateResponse post = postCommandService.createPost(siteUser, postCreateRequest, imageFile);
        return ResponseEntity.ok().body(post);
    }

    @PatchMapping(value = "/{post_id}")
    public ResponseEntity<?> updatePost(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("post_id") Long postId,
            @Valid @RequestPart("postUpdateRequest") PostUpdateRequest postUpdateRequest,
            @RequestParam(value = "file", required = false) List<MultipartFile> imageFile
    ) {
        if (imageFile == null) {
            imageFile = Collections.emptyList();
        }
        PostUpdateResponse postUpdateResponse = postCommandService.updatePost(
                siteUser, postId, postUpdateRequest, imageFile
        );
        return ResponseEntity.ok().body(postUpdateResponse);
    }

    @GetMapping("/{post_id}")
    public ResponseEntity<?> findPostById(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("post_id") Long postId
    ) {
        PostFindResponse postFindResponse = postQueryService.findPostById(siteUser, postId);
        return ResponseEntity.ok().body(postFindResponse);
    }

    @DeleteMapping(value = "/{post_id}")
    public ResponseEntity<?> deletePostById(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("post_id") Long postId
    ) {
        PostDeleteResponse postDeleteResponse = postCommandService.deletePostById(siteUser, postId);
        return ResponseEntity.ok().body(postDeleteResponse);
    }

    @PostMapping(value = "/{post_id}/like")
    public ResponseEntity<?> likePost(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("post_id") Long postId
    ) {
        PostLikeResponse postLikeResponse = postLikeService.likePost(siteUser, postId);
        return ResponseEntity.ok().body(postLikeResponse);
    }

    @DeleteMapping(value = "/{post_id}/like")
    public ResponseEntity<?> dislikePost(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("post_id") Long postId
    ) {
        PostDislikeResponse postDislikeResponse = postLikeService.dislikePost(siteUser, postId);
        return ResponseEntity.ok().body(postDislikeResponse);
    }
}
