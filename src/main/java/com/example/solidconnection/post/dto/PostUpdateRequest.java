package com.example.solidconnection.post.dto;

import lombok.Getter;

public record PostUpdateRequest(
        @Getter
        String postCategory,
        @Getter
        String title,
        @Getter
        String content
) {
}
