package com.example.solidconnection.auth.dto.oauth;

/*
* 애플로부터 사용자의 정보를 받아올 때 사용한다.
* 카카오와 달리 애플은 더 엄격하게 사용자 정보를 관리하여, 이름이나 프로필 이미지 url 을 제공하지 않는다.
* 따라서 닉네임, 프로필 정보는 null 을 반환한다.
* */
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
