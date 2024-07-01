package com.example.solidconnection.type;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public enum RegionCode {
    ASIA("아시아권"),
    AMERICAS("미주권"),
    CHINA("중국권"),
    EUROPE("유럽권");

    private static final Map<String, RegionCode> CACHE = new HashMap<>();

    private final String koreanName;

    static {
        for (RegionCode region : RegionCode.values()) {
            CACHE.put(region.getKoreanName(), region);
        }
    }

    RegionCode(String koreanName) {
        this.koreanName = koreanName;
    }

    public static RegionCode getRegionCodeByKoreanName(String koreanName) {
        return Optional.ofNullable(CACHE.get(koreanName))
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_NAME));
    }
}
