package com.example.solidconnection.auth.dto.oauth;

public record SignUpPrepareResponse(
        boolean isRegistered,
        String nickname,
        String email,
        String profileImageUrl,
        String kakaoOauthToken) implements OAuthResponse {

    public static SignUpPrepareResponse of(OAuthUserInfoDto oAuthUserInfoDto, String kakaoOauthToken) {
        return new SignUpPrepareResponse(
                false,
                oAuthUserInfoDto.getNickname(),
                oAuthUserInfoDto.getEmail(),
                oAuthUserInfoDto.getProfileImageUrl(),
                kakaoOauthToken
        );
    }
}
