package com.example.solidconnection.auth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoUserInfoDto(
        @JsonProperty("kakao_account") KakaoAccountDto kakaoAccountDto) {

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
}
