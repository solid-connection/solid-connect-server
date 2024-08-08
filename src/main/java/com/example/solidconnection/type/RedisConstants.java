package com.example.solidconnection.type;

import lombok.Getter;

@Getter
public enum RedisConstants {
    VIEW_COUNT_TTL("60"),
    VIEW_COUNT_KEY_PREFIX("post:view:"),
    VIEW_COUNT_KEY_PATTERN("post:view:*");

    private final String value;

    RedisConstants(String value) {
        this.value = value;
    }
}
