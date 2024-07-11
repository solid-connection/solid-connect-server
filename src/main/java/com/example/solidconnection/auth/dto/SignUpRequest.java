package com.example.solidconnection.auth.dto;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "회원가입 요청 데이터")
public record SignUpRequest(
        @Schema(description = "카카오 인증 토큰", example = "kakaoToken123")
        String kakaoOauthToken,

        @ArraySchema(schema = @Schema(description = "관심 지역 목록", example = "[\"아시아\", \"유럽\"]"))
        List<String> interestedRegions,

        @ArraySchema(schema = @Schema(description = "관심 국가 목록", example = "[\"일본\", \"독일\"]"))
        List<String> interestedCountries,

        @Schema(description = "지원 준비 단계", example = "CONSIDERING")
        PreparationStatus preparationStatus,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Schema(description = "닉네임", example = "nickname123")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "http://example.com/profile.jpg")
        String profileImageUrl,

        @Schema(description = "성별", example = "MALE")
        Gender gender,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "생년월일", example = "1999-01-01")
        String birth) {

    public SiteUser toSiteUser(String email, Role role) {
        return new SiteUser(
                email,
                this.nickname,
                this.profileImageUrl,
                this.birth,
                this.preparationStatus,
                role,
                this.gender
        );
    }
}
