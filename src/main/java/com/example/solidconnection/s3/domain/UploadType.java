package com.example.solidconnection.s3.domain;

import lombok.Getter;

@Getter
public enum UploadType {
    PROFILE("profile"),
    GPA("gpa"),
    LANGUAGE_TEST("language"),
    COMMUNITY("community"),
    NEWS("news"),
    CHAT("chat/files"),
    MENTOR_PROOF("mentor-proof"),
    ;

    private final String type;

    UploadType(String type) {
        this.type = type;
    }
}
