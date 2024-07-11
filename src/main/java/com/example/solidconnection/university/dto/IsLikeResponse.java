package com.example.solidconnection.university.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대학교 '좋아요' 여부")
public record IsLikeResponse(
        @Schema(description = "대학교 '좋아요' 여부", example = "true")
        boolean isLike) {
}
