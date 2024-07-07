package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.entity.SiteUser;
import jakarta.validation.constraints.NotBlank;

import static com.example.solidconnection.constants.validMessage.NICKNAME_NOT_BLANK;

public record MyPageUpdateResponse(
        @NotBlank(message = NICKNAME_NOT_BLANK)
        String nickname,
        String profileImageUrl) {

    public static MyPageUpdateResponse of(SiteUser siteUser){
        return new MyPageUpdateResponse(
                siteUser.getNickname(),
                siteUser.getProfileImageUrl()
        );
    }
}
