package com.example.solidconnection.auth.dto;

public record SignUpResponse(
        String accessToken,
        String refreshToken) {
}
