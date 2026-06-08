package com.example.solidconnection.admin.auth.dto;

import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.domain.AdminRefreshToken;

public record AdminSignInResult(
        String accessToken,
        String adminRefreshToken
) {

    public static AdminSignInResult of(
            AccessToken accessToken,
            AdminRefreshToken adminRefreshToken
    ) {
        return new AdminSignInResult(accessToken.token(), adminRefreshToken.token());
    }
}
