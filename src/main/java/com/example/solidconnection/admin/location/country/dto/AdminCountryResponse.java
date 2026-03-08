package com.example.solidconnection.admin.location.country.dto;

import com.example.solidconnection.location.country.domain.Country;

public record AdminCountryResponse(
        String code,
        String koreanName,
        String regionCode
) {

    public static AdminCountryResponse from(Country country) {
        return new AdminCountryResponse(
                country.getCode(),
                country.getKoreanName(),
                country.getRegionCode()
        );
    }
}
