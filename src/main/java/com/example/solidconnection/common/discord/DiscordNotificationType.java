package com.example.solidconnection.common.discord;

import lombok.Getter;

@Getter
public enum DiscordNotificationType {

    GPA_SCORE("학점 성적"),
    LANGUAGE_TEST_SCORE("어학 성적"),
    MENTOR_APPLICATION("멘토 신청"),
    REPORT("신고"),
    ;

    private final String displayName;

    DiscordNotificationType(String displayName) {
        this.displayName = displayName;
    }
}
