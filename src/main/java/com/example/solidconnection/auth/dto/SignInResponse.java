package com.example.solidconnection.auth.dto;

import com.example.solidconnection.auth.service.AccessToken;
import com.example.solidconnection.auth.service.RefreshToken;

public record SignInResponse(
        String accessToken,
        String refreshToken
) {

    public static SignInResponse of(AccessToken accessToken, RefreshToken refreshToken) {
        return new SignInResponse(accessToken.token(), refreshToken.token());
    }
}
