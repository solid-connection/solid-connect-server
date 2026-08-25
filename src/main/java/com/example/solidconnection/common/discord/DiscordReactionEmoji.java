package com.example.solidconnection.common.discord;

import lombok.Getter;

@Getter
public enum DiscordReactionEmoji {

    APPROVED("✅"),
    REJECTED("❌"),
    ;

    private final String value;

    DiscordReactionEmoji(String value) {
        this.value = value;
    }
}
