package com.example.solidconnection.auth.dto;

import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

import static com.example.solidconnection.constants.validMessage.NICKNAME_NOT_BLANK;

public record SignUpRequest(
        String kakaoOauthToken,
        List<String> interestedRegions,
        List<String> interestedCountries,
        PreparationStatus preparationStatus,
        @NotBlank(message = NICKNAME_NOT_BLANK)
        String nickname,
        String profileImageUrl,
        Gender gender,
        @JsonFormat(pattern = "yyyy-MM-dd") String birth) {

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
