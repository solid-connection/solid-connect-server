package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.Role;

public record MyPageResponse(
        String nickname,
        String profileImageUrl,
        Role role,
        String birth,
        int likedPostCount,
        int likedMentorCount,
        int likedUniversityCount) {

    public static MyPageResponse of(SiteUser siteUser, int likedUniversityCount) {
        return new MyPageResponse(
                siteUser.getNickname(),
                siteUser.getProfileImageUrl(),
                siteUser.getRole(),
                siteUser.getBirth(),
                0, // TODO: 커뮤니티 기능 생기면 업데이트 필요
                0, // TODO: 멘토 기능 생기면 업데이트 필요
                likedUniversityCount
        );
    }
}
