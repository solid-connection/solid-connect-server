package com.example.solidconnection.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답")
public record ReissueResponse(
        @Schema(description = "새로 발급된 액세스 토큰", example = "newAccessToken123")
        String accessToken) {
}
