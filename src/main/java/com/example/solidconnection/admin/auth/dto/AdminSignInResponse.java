package com.example.solidconnection.admin.auth.dto;

public record AdminSignInResponse(
        String accessToken
) {

    public static AdminSignInResponse from(String accessToken) {
        return new AdminSignInResponse(accessToken);
    }
}
