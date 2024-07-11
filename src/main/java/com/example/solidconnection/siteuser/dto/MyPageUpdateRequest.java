package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.SiteUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "마이 페이지 정보 수정 요청")
public record MyPageUpdateRequest(
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Schema(description = "변경할 닉네임", example = "NewNickname")
        String nickname,

        @Schema(description = "변경할 프로필 이미지 URL", example = "http://example.com/new-profile.jpg", nullable = true)
        String profileImageUrl) {

    public static MyPageUpdateRequest from(SiteUser siteUser) {
        return new MyPageUpdateRequest(
                siteUser.getNickname(),
                siteUser.getProfileImageUrl()
        );
    }
}
