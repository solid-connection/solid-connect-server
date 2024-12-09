package com.example.solidconnection.auth.dto.kakao;

public record FirstAccessResponse(
        boolean isRegistered,
        String nickname,
        String email,
        String profileImageUrl,
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
