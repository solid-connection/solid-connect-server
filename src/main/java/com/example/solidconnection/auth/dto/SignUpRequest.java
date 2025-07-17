package com.example.solidconnection.auth.dto;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SignUpRequest(
        String signUpToken,
        List<String> interestedRegions,
        List<String> interestedCountries,

        @JsonProperty("preparationStatus")
        ExchangeStatus exchangeStatus,

        String profileImageUrl,

        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickname) {

    public SiteUser toOAuthSiteUser(String email, AuthType authType) {
        return new SiteUser(
                email,
                this.nickname,
                this.profileImageUrl,
                this.exchangeStatus,
                Role.MENTEE,
                authType
        );
    }

    public SiteUser toEmailSiteUser(String email, String encodedPassword) {
        return new SiteUser(
                email,
                this.nickname,
                this.profileImageUrl,
                this.exchangeStatus,
                Role.MENTEE,
                AuthType.EMAIL,
                encodedPassword
        );
    }
}
