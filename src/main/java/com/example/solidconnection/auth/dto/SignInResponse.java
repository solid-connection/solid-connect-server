package com.example.solidconnection.auth.dto;

import com.example.solidconnection.auth.dto.kakao.KakaoOauthResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답 데이터")
public record SignInResponse(
        @Schema(description = "사용자 등록 여부", example = "true")
        boolean isRegistered,

        @Schema(description = "발급된 액세스 토큰", example = "accessTokenExample123")
        String accessToken,

        @Schema(description = "발급된 리프레시 토큰", example = "refreshTokenExample123")
        String refreshToken) implements KakaoOauthResponse {
}
