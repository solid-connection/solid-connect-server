package com.example.solidconnection.university.repository.custom;

import com.example.solidconnection.university.domain.UnivApplyInfo;
import java.util.List;

public interface UnivApplyInfoFilterRepository {

    List<UnivApplyInfo> findAllByRegionCodeAndKeywordsAndTermIdAndHomeUniversityId(
            String regionCode,
            List<String> keywords,
            Long term,
            long homeUniversityId);

    List<UnivApplyInfo> findAllByText(String text, Long termId, Long homeUniversityId);
}
