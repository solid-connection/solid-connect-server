package com.example.solidconnection.auth.dto.oauth;

public record OAuthSignInResponse(
        boolean isRegistered,
        String accessToken,
        String refreshToken) implements OAuthResponse {
}
