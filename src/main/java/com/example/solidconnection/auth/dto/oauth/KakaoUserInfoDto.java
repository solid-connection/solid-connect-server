package com.example.solidconnection.auth.dto.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoUserInfoDto(
        @JsonProperty("kakao_account") KakaoAccountDto kakaoAccountDto) implements OAuthUserInfoDto {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoAccountDto(
            @JsonProperty("profile") KakaoProfileDto profile,
            String email) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record KakaoProfileDto(
                @JsonProperty("profile_image_url") String profileImageUrl,
                String nickname) {

        }
    }

    @Override
    public String getEmail() {
        return kakaoAccountDto.email;
    }

    @Override
    public String getProfileImageUrl() {
        return kakaoAccountDto.profile.profileImageUrl;
    }

    @Override
    public String getNickname() {
        return kakaoAccountDto.profile.nickname;
    }
}
