package com.example.solidconnection.community.post.service;

import lombok.Getter;

@Getter
public enum RedisConstants {
    VIEW_COUNT_TTL("60"),
    VALIDATE_VIEW_COUNT_TTL("1"),
    VALIDATE_VIEW_COUNT_KEY_PREFIX("validate:post:view:"),
    VIEW_COUNT_KEY_PREFIX("post:view:"),
    VIEW_COUNT_KEY_PATTERN("post:view:*"),

    REFRESH_LIMIT_PERCENT("10.0"),
    CREATE_LOCK_PREFIX("create_lock:"),
    REFRESH_LOCK_PREFIX("refresh_lock:"),
    LOCK_TIMEOUT_MS("10000"),
    MAX_WAIT_TIME_MS("3000"),
    CREATE_CHANNEL("create_channel");

    private final String value;

    RedisConstants(String value) {
        this.value = value;
    }
}
