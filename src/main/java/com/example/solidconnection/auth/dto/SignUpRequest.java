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
        Gender gender,

        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickname,

        @JsonFormat(pattern = "yyyy-MM-dd")
        String birth) {

    public SiteUser toSiteUser(String email, AuthType authType) {
        return new SiteUser(
                email,
                this.nickname,
                this.profileImageUrl,
                this.birth,
                this.preparationStatus,
                Role.MENTEE,
                this.gender,
                authType
        );
    }
}
