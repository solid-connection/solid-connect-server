package com.example.solidconnection.auth.dto;

import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.domain.RefreshToken;

public record SignInResponse(
        String accessToken,
        String refreshToken
) {

    public static SignInResponse of(AccessToken accessToken, RefreshToken refreshToken) {
        return new SignInResponse(accessToken.token(), refreshToken.token());
    }
}
