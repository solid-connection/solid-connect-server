package com.example.solidconnection.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 후 응답 데이터")
public record SignUpResponse(
        @Schema(description = "액세스 토큰", example = "accessTokenSignup123")
        String accessToken,

        @Schema(description = "리프레시 토큰", example = "refreshTokenSignup123")
        String refreshToken) {
}
