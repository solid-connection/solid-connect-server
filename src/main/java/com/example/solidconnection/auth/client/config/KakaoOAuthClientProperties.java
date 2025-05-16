package com.example.solidconnection.auth.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOAuthClientProperties(
        String tokenUrl,
        String userInfoUrl,
        String redirectUrl,
        String clientId
) {
}
