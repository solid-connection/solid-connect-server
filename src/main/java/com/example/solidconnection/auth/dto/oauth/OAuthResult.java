package com.example.solidconnection.auth.dto.oauth;

public record OAuthResult(
        OAuthResponse response,
        String refreshToken
) {

}
