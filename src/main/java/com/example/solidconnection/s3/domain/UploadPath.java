package com.example.solidconnection.s3.domain;

import lombok.Getter;

@Getter
public enum UploadPath {
    PROFILE("profile"),
    GPA("gpa"),
    LANGUAGE_TEST("language"),
    COMMUNITY("community"),
    NEWS("news"),
    CHAT("chat/files"),
    MENTOR_PROOF("mentor-proof"),
    ;

    private final String type;

    UploadPath(String type) {
        this.type = type;
    }
}
