package com.example.solidconnection.admin.university.dto;

import com.example.solidconnection.university.domain.SemesterAvailableForDispatch;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.LanguageRequirementResponse;
import java.util.List;
import java.util.Map;

public record AdminUnivApplyInfoResponse(
        Long id,
        long termId,
        Long homeUniversityId,
        Long hostUniversityId,
        String koreanName,
        Integer studentCapacity,
        SemesterAvailableForDispatch semesterAvailableForDispatch,
        String semesterRequirement,
        String detailsForLanguage,
        String gpaRequirement,
        String gpaRequirementCriteria,
        String detailsForAccommodation,
        Map<String, String> extraInfo,
        List<LanguageRequirementResponse> languageRequirements
) {

    public static AdminUnivApplyInfoResponse from(UnivApplyInfo univApplyInfo) {
        return new AdminUnivApplyInfoResponse(
                univApplyInfo.getId(),
                univApplyInfo.getTermId(),
                univApplyInfo.getHomeUniversity() != null ? univApplyInfo.getHomeUniversity().getId() : null,
                univApplyInfo.getUniversity().getId(),
                univApplyInfo.getKoreanName(),
                univApplyInfo.getStudentCapacity(),
                univApplyInfo.getSemesterAvailableForDispatch(),
                univApplyInfo.getSemesterRequirement(),
                univApplyInfo.getDetailsForLanguage(),
                univApplyInfo.getGpaRequirement(),
                univApplyInfo.getGpaRequirementCriteria(),
                univApplyInfo.getDetailsForAccommodation(),
                univApplyInfo.getExtraInfo(),
                univApplyInfo.getLanguageRequirements().stream()
                        .map(LanguageRequirementResponse::from)
                        .sorted()
                        .toList()
        );
    }
}
