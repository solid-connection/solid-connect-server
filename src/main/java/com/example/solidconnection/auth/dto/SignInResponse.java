package com.example.solidconnection.auth.dto;

import com.example.solidconnection.auth.dto.kakao.KakaoOauthResponse;

public record SignInResponse(
        boolean isRegistered,
        String accessToken,
        String refreshToken) implements KakaoOauthResponse {
}
