package com.example.solidconnection.auth.dto;

public record SignInResponse(
        String accessToken
) {

    public static SignInResponse from(SignInResult signInResult) {
        return new SignInResponse(signInResult.accessToken());
    }
}
