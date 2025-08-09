package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MyPageResponse(
        String nickname,
        String profileImageUrl,
        Role role,
        AuthType authType,
        String email,
        int likedPostCount,
        int likedMentorCount,

        @JsonProperty("likedUniversityCount")
        int likedUnivApplyInfoCount,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<String> interestedCountries,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String attendedUniversity) {

    public static MyPageResponse of(SiteUser siteUser, int likedUnivApplyInfoCount, List<String> interestedCountries, String attendedUniversity) {
        return new MyPageResponse(
                siteUser.getNickname(),
                siteUser.getProfileImageUrl(),
                siteUser.getRole(),
                siteUser.getAuthType(),
                siteUser.getEmail(),
                0, // TODO: 커뮤니티 기능 생기면 업데이트 필요
                0, // TODO: 멘토 기능 생기면 업데이트 필요
                likedUnivApplyInfoCount,
                interestedCountries,
                attendedUniversity
        );
    }
}
