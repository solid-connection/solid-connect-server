package com.example.solidconnection.admin.university.dto;

import com.example.solidconnection.university.domain.SemesterAvailableForDispatch;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public record AdminUnivApplyInfoUpdateRequest(
        Integer studentCapacity,
        SemesterAvailableForDispatch semesterAvailableForDispatch,
        String semesterRequirement,
        String detailsForLanguage,
        String gpaRequirement,
        String gpaRequirementCriteria,
        String detailsForAccommodation,
        Map<String, String> extraInfo,
        @Valid List<@NotNull AdminUnivApplyInfoLanguageRequirementRequest> languageRequirements
) {
}
