package com.example.solidconnection.auth.dto.kakao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "등록되지 않은 사용자의 최초 접속 시 응답 데이터")
public record FirstAccessResponse(

        @Schema(description = "사용자 등록 여부", example = "false")
        boolean isRegistered,

        @Schema(description = "카카오 닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "카카오 프로필 이미지 URL", example = "http://example.com/image.jpg")
        String profileImageUrl,

        @Schema(description = "우리 서비스에사 발급한 카카오 인증 토큰", example = "abc123xyz")
        String kakaoOauthToken) implements KakaoOauthResponse {

    public static FirstAccessResponse of(KakaoUserInfoDto kakaoUserInfoDto, String kakaoOauthToken) {
        return new FirstAccessResponse(
                false,
                kakaoUserInfoDto.kakaoAccountDto().profile().nickname(),
                kakaoUserInfoDto.kakaoAccountDto().email(),
                kakaoUserInfoDto.kakaoAccountDto().profile().profileImageUrl(),
                kakaoOauthToken
        );
    }
}
