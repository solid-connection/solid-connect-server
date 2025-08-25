package com.example.solidconnection.auth.controller;

import static com.example.solidconnection.common.exception.ErrorCode.REFRESH_TOKEN_NOT_EXISTS;

import com.example.solidconnection.auth.controller.config.RefreshTokenCookieProperties;
import com.example.solidconnection.auth.token.config.TokenProperties;
import com.example.solidconnection.common.exception.CustomException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieManager {

    private static final String COOKIE_NAME = "refreshToken";
    private static final String PATH = "/";

    private final RefreshTokenCookieProperties properties;
    private final TokenProperties tokenProperties;

    public void setCookie(HttpServletResponse response, String refreshToken) {
        Duration tokenExpireTime = tokenProperties.refresh().expireTime();
        long cookieMaxAge = convertExpireTimeToCookieMaxAge(tokenExpireTime);
        setRefreshTokenCookie(response, refreshToken, cookieMaxAge);
    }

    private long convertExpireTimeToCookieMaxAge(Duration tokenExpireTime) {
        // jwt의 expireTime 단위인 millisecond를 cookie의 maxAge 단위인 second로 변환
        return tokenExpireTime.toSeconds();
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
                .sameSite(SameSite.LAX.attributeValue())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public String getRefreshToken(HttpServletRequest request) {
        // 쿠키가 없거나 비어있는 경우 예외 발생
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            throw new CustomException(REFRESH_TOKEN_NOT_EXISTS);
        }

        // refreshToken 쿠키가 없는 경우 예외 발생
        Cookie refreshTokenCookie = Arrays.stream(cookies)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .orElseThrow(() -> new CustomException(REFRESH_TOKEN_NOT_EXISTS));

        // 쿠키 값이 비어있는 경우 예외 발생
        String refreshToken = refreshTokenCookie.getValue();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(REFRESH_TOKEN_NOT_EXISTS);
        }
        return refreshToken;
    }
}
