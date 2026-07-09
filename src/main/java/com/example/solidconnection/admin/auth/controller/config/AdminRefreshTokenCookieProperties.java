package com.example.solidconnection.admin.auth.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "token.admin-refresh")
public record AdminRefreshTokenCookieProperties(
        String cookieName,
        String cookieDomain
) {

}
