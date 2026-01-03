package com.example.solidconnection.admin.location.region.dto;

import com.example.solidconnection.location.region.domain.Region;

public record AdminRegionResponse(
        String code,
        String koreanName
) {

    public static AdminRegionResponse from(Region region) {
        return new AdminRegionResponse(
                region.getCode(),
                region.getKoreanName()
        );
    }
}
