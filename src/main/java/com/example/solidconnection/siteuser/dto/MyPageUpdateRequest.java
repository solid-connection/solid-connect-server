package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.constraints.NotBlank;

public record MyPageUpdateRequest(
        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickname,
        String profileImageUrl) {

    public static MyPageUpdateRequest from(SiteUser siteUser) {
        return new MyPageUpdateRequest(
                siteUser.getNickname(),
                siteUser.getProfileImageUrl()
        );
    }
}
