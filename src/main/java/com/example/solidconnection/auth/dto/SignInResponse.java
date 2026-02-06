package com.example.solidconnection.auth.dto;

import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.domain.RefreshToken;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record SignInResponse(
        String accessToken,
        @JsonIgnore String refreshToken
) {

    public static SignInResponse of(AccessToken accessToken, RefreshToken refreshToken) {
        return new SignInResponse(accessToken.token(), refreshToken.token());
    }
}
