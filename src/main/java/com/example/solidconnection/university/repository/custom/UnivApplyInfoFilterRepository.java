package com.example.solidconnection.university.repository.custom;

import com.example.solidconnection.university.domain.UnivApplyInfo;
import java.util.List;

public interface UnivApplyInfoFilterRepository {

    List<UnivApplyInfo> findAllByRegionCodeAndKeywordsAndTermId(String regionCode, List<String> keywords, Long term);

    List<UnivApplyInfo> findAllByText(String text, Long termId);
}
