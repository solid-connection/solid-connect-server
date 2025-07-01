package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MyPageResponse(
        String nickname,
        String profileImageUrl,
        Role role,
        AuthType authType,
        String email,
        int likedPostCount,
        int likedMentorCount,

        @JsonProperty("likedUniversityCount")
        int likedUnivApplyInfoCount) {

    public static MyPageResponse of(SiteUser siteUser, int likedUnivApplyInfoCount) {
        return new MyPageResponse(
                siteUser.getNickname(),
                siteUser.getProfileImageUrl(),
                siteUser.getRole(),
                siteUser.getAuthType(),
                siteUser.getEmail(),
                0, // TODO: 커뮤니티 기능 생기면 업데이트 필요
                0, // TODO: 멘토 기능 생기면 업데이트 필요
                likedUnivApplyInfoCount
        );
    }
}
