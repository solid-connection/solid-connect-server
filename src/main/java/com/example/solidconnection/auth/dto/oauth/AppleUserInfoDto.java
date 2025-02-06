package com.example.solidconnection.auth.dto.oauth;

public record AppleUserInfoDto(String email) implements OAuthUserInfoDto {

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getProfileImageUrl() {
        return null;
    }

    @Override
    public String getNickname() {
        return null;
    }
}
