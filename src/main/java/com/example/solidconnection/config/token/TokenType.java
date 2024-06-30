package com.example.solidconnection.config.token;

import lombok.Getter;

@Getter
public enum TokenType {
    ACCESS("", 1000 * 60 * 60),
    REFRESH("refresh:", 1000 * 60 * 60 * 24 * 7),
    KAKAO_OAUTH("kakao:", 1000 * 60 * 60);

    private final String prefix;
    private final int expireTime;

    TokenType(String prefix, int expireTime){
        this.prefix = prefix;
        this.expireTime = expireTime;
    }

    public String createTokenKey(String subject) {
        return prefix + subject;
    }
}
