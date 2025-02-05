package com.example.solidconnection.config.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.apple")
public record AppleOAuthClientProperties(
        String tokenUrl,
        String clientSecretAudienceUrl,
        String redirectUrl,
        String clientId,
        String teamId,
        String keyId
) {
}
