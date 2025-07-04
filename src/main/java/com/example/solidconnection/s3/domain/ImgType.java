package com.example.solidconnection.s3.domain;

import lombok.Getter;

@Getter
public enum ImgType {
    PROFILE("profile"), GPA("gpa"), LANGUAGE_TEST("language"), COMMUNITY("community"), NEWS("news");

    private final String type;

    ImgType(String type) {
        this.type = type;
    }
}
