package com.example.solidconnection.admin.auth.controller;

import static com.example.solidconnection.common.exception.ErrorCode.ADMIN_REFRESH_TOKEN_NOT_EXISTS;

import com.example.solidconnection.admin.auth.controller.config.AdminRefreshTokenCookieProperties;
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
public class AdminRefreshTokenCookieManager {

    private static final String PATH = "/";

    private final AdminRefreshTokenCookieProperties properties;
    private final TokenProperties tokenProperties;

    public void setCookie(HttpServletResponse response, String adminRefreshToken) {
        Duration tokenExpireTime = tokenProperties.adminRefresh().expireTime();
        long cookieMaxAge = tokenExpireTime.toSeconds();
        setAdminRefreshTokenCookie(response, adminRefreshToken, cookieMaxAge);
    }

    public void deleteCookie(HttpServletResponse response) {
        setAdminRefreshTokenCookie(response, "", 0);
    }

    private void setAdminRefreshTokenCookie(
            HttpServletResponse response, String adminRefreshToken, long maxAge
    ) {
        ResponseCookie cookie = ResponseCookie.from(properties.cookieName(), adminRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path(PATH)
                .maxAge(maxAge)
                .domain(properties.cookieDomain())
                .sameSite(SameSite.LAX.attributeValue())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public String getAdminRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            throw new CustomException(ADMIN_REFRESH_TOKEN_NOT_EXISTS);
        }

        Cookie adminRefreshTokenCookie = Arrays.stream(cookies)
                .filter(cookie -> properties.cookieName().equals(cookie.getName()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ADMIN_REFRESH_TOKEN_NOT_EXISTS));

        String adminRefreshToken = adminRefreshTokenCookie.getValue();
        if (adminRefreshToken == null || adminRefreshToken.isBlank()) {
            throw new CustomException(ADMIN_REFRESH_TOKEN_NOT_EXISTS);
        }
        return adminRefreshToken;
    }
}
