package com.example.solidconnection.auth.controller;

import com.example.solidconnection.auth.domain.TokenType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieManager {

    private static final String COOKIE_NAME = "refreshToken";
    private static final String PATH = "/";
    private static final String SAME_SITE = "Strict";

    public void setCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .path(PATH)
                .maxAge(changeMilliSecondToSecond(TokenType.REFRESH.getExpireTime()))  // 초단위
                .sameSite(SAME_SITE)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private long changeMilliSecondToSecond(long milliSeconds) {
        return milliSeconds / 1000;
    }

    public void deleteCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path(PATH)
                .maxAge(0) // 쿠키 삭제를 위해 maxAge를 0으로 설정
                .sameSite(SAME_SITE)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
