package com.example.solidconnection.university.repository.custom;

import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import java.util.List;

public interface UnivApplyInfoFilterRepository {

    List<UnivApplyInfo> findAllByRegionCodeAndKeywords(String regionCode, List<String> keywords);

    List<UnivApplyInfo> findAllByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScoreAndTerm(
            String regionCode, List<String> keywords, LanguageTestType testType, String testScore, String term);
}
