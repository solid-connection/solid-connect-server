package com.example.solidconnection.university.repository.custom;

import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;

import java.util.List;

public interface UniversityFilterRepository {

    List<University> findByRegionCodeAndKeywords(String regionCode, List<String> keywords);

    List<UniversityInfoForApply> findByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScore(
            String regionCode, List<String> keywords, LanguageTestType testType, String testScore);
}
