package com.example.solidconnection.type;

import lombok.Getter;

@Getter
public enum ImgType {
    PROFILE("profile"), GPA("gpa"), LANGUAGE_TEST("language");

    private final String type;

    ImgType(String type) {
        this.type = type;
    }
}
