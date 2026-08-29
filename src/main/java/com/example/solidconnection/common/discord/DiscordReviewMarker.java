package com.example.solidconnection.common.discord;

import lombok.Getter;

@Getter
public enum DiscordReviewMarker {

    APPROVED("✅"),
    REJECTED("❌"),
    ;

    private final String value;

    DiscordReviewMarker(String value) {
        this.value = value;
    }
}
