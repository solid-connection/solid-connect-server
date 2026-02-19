package com.example.solidconnection.admin.university.dto;

import com.example.solidconnection.university.domain.HostUniversity;

public record AdminHostUniversityResponse(
        Long id,
        String koreanName,
        String englishName,
        String formatName,
        String logoImageUrl,
        String countryCode,
        String countryKoreanName,
        String regionCode,
        String regionKoreanName
) {

    public static AdminHostUniversityResponse from(HostUniversity hostUniversity) {
        return new AdminHostUniversityResponse(
                hostUniversity.getId(),
                hostUniversity.getKoreanName(),
                hostUniversity.getEnglishName(),
                hostUniversity.getFormatName(),
                hostUniversity.getLogoImageUrl(),
                hostUniversity.getCountry() != null ? hostUniversity.getCountry().getCode() : null,
                hostUniversity.getCountry() != null ? hostUniversity.getCountry().getKoreanName() : null,
                hostUniversity.getRegion() != null ? hostUniversity.getRegion().getCode() : null,
                hostUniversity.getRegion() != null ? hostUniversity.getRegion().getKoreanName() : null
        );
    }
}
