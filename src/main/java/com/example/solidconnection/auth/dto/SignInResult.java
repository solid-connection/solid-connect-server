package com.example.solidconnection.auth.dto;

import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.domain.RefreshToken;

public record SignInResult(
        String accessToken,
        String refreshToken
) {

    public static SignInResult of(AccessToken accessToken, RefreshToken refreshToken) {
        return new SignInResult(accessToken.token(), refreshToken.token());
    }
}
