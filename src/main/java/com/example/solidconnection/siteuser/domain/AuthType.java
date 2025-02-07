package com.example.solidconnection.siteuser.domain;

public enum AuthType {

    KAKAO,
    APPLE,
    EMAIL,
    ;

    public static boolean isEmail(AuthType authType) {
        return EMAIL.equals(authType);
    }
}
