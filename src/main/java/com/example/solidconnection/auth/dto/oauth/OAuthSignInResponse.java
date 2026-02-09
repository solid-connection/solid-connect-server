package com.example.solidconnection.auth.dto.oauth;

import com.example.solidconnection.auth.dto.SignInResult;

public record OAuthSignInResponse(
        String accessToken) implements OAuthResponse {

    public static OAuthSignInResponse from(SignInResult signInResult) {
        return new OAuthSignInResponse(signInResult.accessToken());
    }
}
