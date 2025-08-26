package com.example.solidconnection.auth.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.apple")
public record AppleOAuthClientProperties(
        String tokenUrl,
        String clientSecretAudienceUrl,
        String redirectUrl,
        String publicKeyUrl,
        String clientId,
        String teamId,
        String keyId,
        String secretKey
) {

}
