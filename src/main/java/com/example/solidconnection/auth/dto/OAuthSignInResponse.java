package com.example.solidconnection.auth.dto;

import com.example.solidconnection.auth.dto.kakao.KakaoOAuthResponse;

public record OAuthSignInResponse(
        boolean isRegistered,
        String accessToken,
        String refreshToken) implements KakaoOAuthResponse {
}
