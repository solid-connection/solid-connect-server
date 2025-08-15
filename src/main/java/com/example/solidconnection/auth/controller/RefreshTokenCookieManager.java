package com.example.solidconnection.auth.controller;

import com.example.solidconnection.auth.controller.config.RefreshTokenCookieProperties;
import com.example.solidconnection.auth.domain.TokenType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieManager {

    private static final String COOKIE_NAME = "refreshToken";
    private static final String PATH = "/";

    private final RefreshTokenCookieProperties properties;

    public void setCookie(HttpServletResponse response, String refreshToken) {
        long maxAge = convertExpireTimeToCookieMaxAge(TokenType.REFRESH.getExpireTime());
        setRefreshTokenCookie(response, refreshToken, maxAge);
    }

    private long convertExpireTimeToCookieMaxAge(long milliSeconds) {
        // jwt의 expireTime 단위인 millisecond를 cookie의 maxAge 단위인 second로 변환
        return milliSeconds / 1000;
    }

    public void deleteCookie(HttpServletResponse response) {
        setRefreshTokenCookie(response, "", 0); // 쿠키 삭제를 위해 maxAge를 0으로 설정
    }

    private void setRefreshTokenCookie(
            HttpServletResponse response, String refreshToken, long maxAge
    ) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .path(PATH)
                .maxAge(maxAge)
                .domain(properties.cookieDomain())
                .sameSite(properties.sameSite())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
