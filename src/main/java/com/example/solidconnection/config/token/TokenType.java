package com.example.solidconnection.config.token;

import lombok.Getter;

@Getter
public enum TokenType {

    ACCESS("", 1000 * 60 * 60), // 1hour
    REFRESH("refresh:", 1000 * 60 * 60 * 24 * 7), // 7days
    KAKAO_OAUTH("kakao:", 1000 * 60 * 60); // 1hour

    private final String prefix;
    private final int expireTime;

    TokenType(String prefix, int expireTime) {
        this.prefix = prefix;
        this.expireTime = expireTime;
    }

    public String addPrefixToSubject(String subject) {
        return prefix + subject;
    }
}
