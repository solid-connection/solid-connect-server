package com.example.solidconnection.university.fixture;

import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;

import java.util.Set;

public record UniversityData(
        Region 지역,
        Country 국가,
        University 대학,
        UniversityInfoForApply 대학_지원_정보,
        Set<LanguageRequirement> 언어_요구사항들
) {
}
