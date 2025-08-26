package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.SiteUser;

public record ProfileImageUpdateResponse(
        String profileImageUrl
) {

    public static ProfileImageUpdateResponse from(SiteUser siteUser) {
        return new ProfileImageUpdateResponse(
                siteUser.getProfileImageUrl()
        );
    }
}
