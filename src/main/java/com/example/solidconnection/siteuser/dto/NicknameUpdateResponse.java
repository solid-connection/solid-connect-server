package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.SiteUser;

public record NicknameUpdateResponse(
        String nickname
) {
    public static NicknameUpdateResponse from(SiteUser siteUser) {
        return new NicknameUpdateResponse(
                siteUser.getNickname()
        );
    }
}
