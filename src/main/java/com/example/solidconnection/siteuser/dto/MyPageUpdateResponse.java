package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.SiteUser;

public record MyPageUpdateResponse(
        String nickname,
        String profileImageUrl) {

    public static MyPageUpdateResponse from(SiteUser siteUser) {
        return new MyPageUpdateResponse(
                siteUser.getNickname(),
                siteUser.getProfileImageUrl()
        );
    }
}
