package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.SiteUser;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마이 페이지 정보 수정 응답")
public record MyPageUpdateResponse(
        @Schema(description = "업데이트된 사용자 닉네임", example = "UpdatedNickname")
        String nickname,

        @Schema(description = "업데이트된 프로필 이미지 URL", example = "http://example.com/updated-profile.jpg")
        String profileImageUrl) {

        public static MyPageUpdateResponse from(SiteUser siteUser) {
                return new MyPageUpdateResponse(
                        siteUser.getNickname(),
                        siteUser.getProfileImageUrl()
                );
        }
}
