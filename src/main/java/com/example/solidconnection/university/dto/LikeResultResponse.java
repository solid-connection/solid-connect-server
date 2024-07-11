package com.example.solidconnection.university.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요 결과 응답 데이터")
public record LikeResultResponse(
        @Schema(description = "좋아요 결과", example = "LIKE_SUCCESS")
        String result) {
}
