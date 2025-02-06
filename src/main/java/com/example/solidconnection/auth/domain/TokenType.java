package com.example.solidconnection.auth.domain;

import lombok.Getter;

@Getter
public enum TokenType {

    ACCESS("ACCESS:", 1000 * 60 * 60), // 1hour
    REFRESH("REFRESH:", 1000 * 60 * 60 * 24 * 7), // 7days
    BLACKLIST("BLACKLIST:", ACCESS.expireTime),
    SIGN_UP("SIGN_UP:", 1000 * 60 * 10), // 10min
    ;

    private final String prefix;
    private final int expireTime;

    TokenType(String prefix, int expireTime) {
        this.prefix = prefix;
        this.expireTime = expireTime;
    }

    public String addPrefix(String string) {
        return prefix + string;
    }
}
