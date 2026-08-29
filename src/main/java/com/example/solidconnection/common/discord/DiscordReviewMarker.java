package com.example.solidconnection.common.discord;

import lombok.Getter;

@Getter
public enum DiscordReviewMarker {

    APPROVED("(승인되었습니다.)"),
    REJECTED("(반려되었습니다.)"),
    ;

    private final String value;

    DiscordReviewMarker(String value) {
        this.value = value;
    }
}
