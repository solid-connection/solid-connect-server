package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.SiteUser;

public record PostFindSiteUserResponse(
        Long id,
        String nickname,
        String profileImageUrl
) {

    public static PostFindSiteUserResponse from(SiteUser siteUser) {
        return new PostFindSiteUserResponse(
                siteUser.getId(),
                siteUser.getNickname(),
                siteUser.getProfileImageUrl()
        );
    }
}
