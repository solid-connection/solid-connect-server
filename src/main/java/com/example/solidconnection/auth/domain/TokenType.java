package com.example.solidconnection.auth.domain;

import lombok.Getter;

@Getter
public enum TokenType {

    ACCESS("ACCESS:", 1000 * 60 * 60), // 1hour
    REFRESH("REFRESH:", 1000 * 60 * 60 * 24 * 7), // 7days
    KAKAO_OAUTH("KAKAO:", 1000 * 60 * 60), // 1hour
    BLACKLIST("BLACKLIST:", ACCESS.expireTime)
    ;

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
