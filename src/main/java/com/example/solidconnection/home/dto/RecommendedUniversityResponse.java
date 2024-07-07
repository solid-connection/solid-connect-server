package com.example.solidconnection.home.dto;

import com.example.solidconnection.entity.UniversityInfoForApply;

public record RecommendedUniversityResponse(
        long id,
        String koreanName,
        String backgroundImgUrl) {

    public static RecommendedUniversityResponse from(UniversityInfoForApply universityInfoForApply){
        return new RecommendedUniversityResponse(
                universityInfoForApply.getId(),
                universityInfoForApply.getUniversity().getKoreanName(),
                universityInfoForApply.getUniversity().getBackgroundImageUrl());
    }
}
