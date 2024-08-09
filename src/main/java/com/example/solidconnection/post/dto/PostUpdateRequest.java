package com.example.solidconnection.post.dto;

public record PostUpdateRequest(
        String postCategory,
        String title,
        String content
) {
}
