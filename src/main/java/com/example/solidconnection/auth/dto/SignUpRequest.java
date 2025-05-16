package com.example.solidconnection.auth.dto;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.PreparationStatus;
import com.example.solidconnection.siteuser.domain.Role;
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
                this.preparationStatus,
                Role.MENTEE,
                authType
        );
    }

    public SiteUser toEmailSiteUser(String email, String encodedPassword) {
        return new SiteUser(
                email,
                this.nickname,
                this.profileImageUrl,
                this.preparationStatus,
                Role.MENTEE,
                AuthType.EMAIL,
                encodedPassword
        );
    }
}
