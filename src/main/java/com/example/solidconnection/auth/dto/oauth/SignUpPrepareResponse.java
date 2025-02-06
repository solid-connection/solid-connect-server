package com.example.solidconnection.auth.dto.oauth;

public record SignUpPrepareResponse(
        boolean isRegistered,
        String nickname,
        String email,
        String profileImageUrl,
        String signUpToken) implements OAuthResponse {

    public static SignUpPrepareResponse of(OAuthUserInfoDto oAuthUserInfoDto, String signUpToken) {
        return new SignUpPrepareResponse(
                false,
                oAuthUserInfoDto.getNickname(),
                oAuthUserInfoDto.getEmail(),
                oAuthUserInfoDto.getProfileImageUrl(),
                signUpToken
        );
    }
}
