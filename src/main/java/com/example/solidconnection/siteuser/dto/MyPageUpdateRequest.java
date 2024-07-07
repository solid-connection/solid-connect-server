package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.entity.SiteUser;
import jakarta.validation.constraints.NotBlank;

import static com.example.solidconnection.constants.validMessage.NICKNAME_NOT_BLANK;

public record MyPageUpdateRequest(
        @NotBlank(message = NICKNAME_NOT_BLANK)
        String nickname,
        String profileImageUrl) {

    public static MyPageUpdateRequest of(SiteUser siteUser){
        return new MyPageUpdateRequest(
                siteUser.getNickname(),
                siteUser.getProfileImageUrl()
        );
    }
}
