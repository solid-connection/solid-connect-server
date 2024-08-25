package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.SiteUser;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileImageUpdateResponse(
        @Schema(description = "업데이트된 프로필 이미지 URL", example = "http://example.com/updated-profile.jpg")
        String profileImageUrl
) {
    public static ProfileImageUpdateResponse from(SiteUser siteUser) {
        return new ProfileImageUpdateResponse(
                siteUser.getProfileImageUrl()
        );
    }
}
