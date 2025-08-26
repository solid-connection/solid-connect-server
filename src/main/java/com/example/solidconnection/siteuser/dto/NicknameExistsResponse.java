package com.example.solidconnection.siteuser.dto;

public record NicknameExistsResponse(
        boolean exists
) {

    public static NicknameExistsResponse from(boolean exists) {
        return new NicknameExistsResponse(exists);
    }
}
