package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.Role;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마이페이지 페이지 정보 응답")
public record MyPageResponse(
        @Schema(description = "닉네임", example = "nickname")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "http://example.com/profile.jpg")
        String profileImageUrl,

        @Schema(description = "역할", example = "MENTEE")
        Role role,

        @Schema(description = "생년월일", example = "1990-01-01")
        String birth,

        @Schema(description = "이메일", example = "example@solid-conenct.net")
        String email,

        @Schema(description = "좋아요 누른 게시물 수", example = "0")
        int likedPostCount,

        @Schema(description = "좋아요 누른 멘토 수", example = "0")
        int likedMentorCount,

        @Schema(description = "좋아요 누른 대학 수", example = "3")
        int likedUniversityCount) {

    public static MyPageResponse of(SiteUser siteUser, int likedUniversityCount) {
        return new MyPageResponse(
                siteUser.getNickname(),
                siteUser.getProfileImageUrl(),
                siteUser.getRole(),
                siteUser.getBirth(),
                siteUser.getEmail(),
                0, // TODO: 커뮤니티 기능 생기면 업데이트 필요
                0, // TODO: 멘토 기능 생기면 업데이트 필요
                likedUniversityCount
        );
    }
}
