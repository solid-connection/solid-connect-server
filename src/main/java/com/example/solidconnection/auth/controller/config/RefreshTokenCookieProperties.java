package com.example.solidconnection.auth.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "token.refresh")
public record RefreshTokenCookieProperties(
        String cookieDomain
) {

}
