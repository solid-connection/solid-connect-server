package com.example.solidconnection.auth.dto.oauth;

import com.example.solidconnection.auth.dto.SignInResult;

public record OAuthSignInResponse(
        boolean isRegistered,
        String accessToken) implements OAuthResponse {

    public static OAuthSignInResponse from(SignInResult signInResult) {
        return new OAuthSignInResponse(true, signInResult.accessToken());
    }
}
