package com.example.solidconnection.university.repository.custom;

import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import java.util.List;

public interface UnivApplyInfoFilterRepository {

    List<UnivApplyInfo> findAllByRegionCodeAndKeywordsAndTermId(String regionCode, List<String> keywords, long term);

    List<UnivApplyInfo> findAllByFilter(LanguageTestType testType, String testScore, Long termId, List<String> countryKoreanNames);

    List<UnivApplyInfo> findAllByText(String text, Long termId);
}
