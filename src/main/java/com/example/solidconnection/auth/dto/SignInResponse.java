package com.example.solidconnection.auth.dto;

public record SignInResponse(
        String accessToken,
        String refreshToken
) {
}
