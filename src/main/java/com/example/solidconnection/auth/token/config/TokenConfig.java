package com.example.solidconnection.auth.token.config;

import java.time.Duration;

public record TokenConfig(
        String storageKeyPrefix,
        Duration expireTime
) {

}
