package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.SiteUser;
import io.swagger.v3.oas.annotations.media.Schema;

public record NicknameUpdateResponse(
        @Schema(description = "업데이트된 사용자 닉네임", example = "UpdatedNickname")
        String nickname
) {
    public static NicknameUpdateResponse from(SiteUser siteUser) {
        return new NicknameUpdateResponse(
                siteUser.getNickname()
        );
    }
}
