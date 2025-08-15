package com.example.solidconnection.auth.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.server.Cookie.SameSite;

@ConfigurationProperties(prefix = "token.refresh")
public record RefreshTokenCookieProperties(
        String cookieDomain
) {

    public String sameSite() {
        if (isDomainSet()) {
            return SameSite.STRICT.attributeValue(); // 도메인을 지정한 경우 SameSite=Strict
        }
        return SameSite.NONE.attributeValue(); // 도메인을 지정하지 않은 경우 SameSite=None
    }

    private boolean isDomainSet() {
        return cookieDomain != null && !cookieDomain.isBlank();
    }
}
