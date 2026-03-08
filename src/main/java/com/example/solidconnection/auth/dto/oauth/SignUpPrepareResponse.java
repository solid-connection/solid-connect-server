package com.example.solidconnection.auth.dto.oauth;

public record SignUpPrepareResponse(
        String nickname,
        String email,
        String profileImageUrl,
        String signUpToken) implements OAuthResponse {

    public static SignUpPrepareResponse of(OAuthUserInfoDto oAuthUserInfoDto, String signUpToken) {
        return new SignUpPrepareResponse(
                oAuthUserInfoDto.getNickname(),
                oAuthUserInfoDto.getEmail(),
                oAuthUserInfoDto.getProfileImageUrl(),
                signUpToken
        );
    }
}
