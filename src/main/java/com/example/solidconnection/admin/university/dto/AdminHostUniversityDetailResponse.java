package com.example.solidconnection.admin.university.dto;

import com.example.solidconnection.university.domain.HostUniversity;

public record AdminHostUniversityDetailResponse(
        Long id,
        String koreanName,
        String englishName,
        String formatName,
        String homepageUrl,
        String englishCourseUrl,
        String accommodationUrl,
        String logoImageUrl,
        String backgroundImageUrl,
        String detailsForLocal,
        String countryCode,
        String countryKoreanName,
        String regionCode,
        String regionKoreanName
) {

    public static AdminHostUniversityDetailResponse from(HostUniversity hostUniversity) {
        return new AdminHostUniversityDetailResponse(
                hostUniversity.getId(),
                hostUniversity.getKoreanName(),
                hostUniversity.getEnglishName(),
                hostUniversity.getFormatName(),
                hostUniversity.getHomepageUrl(),
                hostUniversity.getEnglishCourseUrl(),
                hostUniversity.getAccommodationUrl(),
                hostUniversity.getLogoImageUrl(),
                hostUniversity.getBackgroundImageUrl(),
                hostUniversity.getDetailsForLocal(),
                hostUniversity.getCountry() != null ? hostUniversity.getCountry().getCode() : null,
                hostUniversity.getCountry() != null ? hostUniversity.getCountry().getKoreanName() : null,
                hostUniversity.getRegion() != null ? hostUniversity.getRegion().getCode() : null,
                hostUniversity.getRegion() != null ? hostUniversity.getRegion().getKoreanName() : null
        );
    }
}
