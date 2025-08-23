package com.example.solidconnection.auth.token.config;

public record TokenConfig(
        String storageKeyPrefix,
        long expireTime
) {

}
