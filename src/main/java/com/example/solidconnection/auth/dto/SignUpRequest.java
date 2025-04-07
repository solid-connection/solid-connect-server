package com.example.solidconnection.auth.dto;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SignUpRequest(
        String signUpToken,
        List<String> interestedRegions,
        List<String> interestedCountries,
        PreparationStatus preparationStatus,
        String profileImageUrl,

        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickname) {

    public SiteUser toOAuthSiteUser(String email, AuthType authType) {
        return new SiteUser(
                email,
                this.nickname,
                this.profileImageUrl,
                "2000-01-01",
                this.preparationStatus,
                Role.MENTEE,
                Gender.PREFER_NOT_TO_SAY,
                authType
        );
    }

    public SiteUser toEmailSiteUser(String email, String encodedPassword) {
        return new SiteUser(
                email,
                this.nickname,
                this.profileImageUrl,
                "2000-01-01",
                this.preparationStatus,
                Role.MENTEE,
                Gender.PREFER_NOT_TO_SAY,
                AuthType.EMAIL,
                encodedPassword
        );
    }
}
