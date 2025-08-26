package com.example.solidconnection.auth.domain;

import lombok.Getter;

@Getter
public enum TokenType {

    ACCESS("ACCESS:", 1000L * 60 * 60), // 1hour
    REFRESH("REFRESH:", 1000L * 60 * 60 * 24 * 90), // 90days
    BLACKLIST("BLACKLIST:", ACCESS.expireTime),
    SIGN_UP("SIGN_UP:", 1000L * 60 * 10), // 10min
    ;

    private final String prefix;
    private final long expireTime;

    TokenType(String prefix, long expireTime) {
        this.prefix = prefix;
        this.expireTime = expireTime;
    }

    public String addPrefix(String string) {
        return prefix + string;
    }
}
