package com.example.solidconnection.admin.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.SemesterAvailableForDispatch;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.repository.HostUniversityRepository;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUnivApplyInfoRowSaver {

    private final HostUniversityRepository hostUniversityRepository;
    private final UnivApplyInfoRepository univApplyInfoRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(
            Map<String, String> rowData,
            Map<String, String> columnMappings,
            HomeUniversity homeUniversity,
            long termId
    ) {
        ImportData data = buildImportData(rowData, columnMappings);

        if (data.universityKoreanName == null || data.universityKoreanName.isBlank()) {
            throw new IllegalArgumentException("대학명(universityKoreanName) 컬럼이 매핑되지 않았습니다");
        }

        HostUniversity hostUniversity = hostUniversityRepository
                .findByKoreanName(data.universityKoreanName)
                .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));

        UnivApplyInfo univApplyInfo = new UnivApplyInfo(
                null,
                termId,
                homeUniversity,
                data.universityKoreanName,
                data.studentCapacity,
                null,
                data.semesterAvailableForDispatch,
                data.semesterRequirement,
                data.detailsForLanguage,
                data.gpaRequirement,
                data.gpaRequirementCriteria,
                null,
                null,
                data.detailsForAccommodation,
                null,
                null,
                data.extraInfo.isEmpty() ? null : data.extraInfo,
                new HashSet<>(),
                hostUniversity
        );

        UnivApplyInfo saved = univApplyInfoRepository.save(univApplyInfo);

        data.languageRequirements.forEach((testType, minScore) -> {
            LanguageRequirement lr = new LanguageRequirement(null, testType, minScore, saved);
            saved.addLanguageRequirements(lr);
        });
    }

    private ImportData buildImportData(Map<String, String> rowData, Map<String, String> columnMappings) {
        ImportData data = new ImportData();
        rowData.forEach((header, value) -> applyField(data, header, value, columnMappings));
        return data;
    }

    private void applyField(ImportData data, String header, String value, Map<String, String> columnMappings) {
        String targetField = columnMappings.getOrDefault(header, "extraInfo");

        if ("extraInfo".equals(targetField)) {
            data.extraInfo.put(header, value);
            return;
        }

        try {
            LanguageTestType testType = LanguageTestType.valueOf(targetField);
            if (!value.isBlank()) {
                data.languageRequirements.put(testType, value);
            }
            return;
        } catch (IllegalArgumentException ignored) {
        }

        if (!tryApplyStructuredField(data, targetField, value)) {
            data.extraInfo.put(header, value);
        }
    }

    private boolean tryApplyStructuredField(ImportData data, String fieldName, String value) {
        try {
            switch (fieldName) {
                case "universityKoreanName" -> data.universityKoreanName = value;
                case "studentCapacity" -> data.studentCapacity = Integer.parseInt(value);
                case "semesterAvailableForDispatch" -> data.semesterAvailableForDispatch = SemesterAvailableForDispatch.valueOf(value);
                case "semesterRequirement" -> data.semesterRequirement = value;
                case "detailsForLanguage" -> data.detailsForLanguage = value;
                case "gpaRequirement" -> data.gpaRequirement = value;
                case "gpaRequirementCriteria" -> data.gpaRequirementCriteria = value;
                case "detailsForAccommodation" -> data.detailsForAccommodation = value;
                default -> { return false; }
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static class ImportData {

        String universityKoreanName;
        Integer studentCapacity;
        SemesterAvailableForDispatch semesterAvailableForDispatch;
        String semesterRequirement;
        String detailsForLanguage;
        String gpaRequirement;
        String gpaRequirementCriteria;
        String detailsForAccommodation;
        Map<String, String> extraInfo = new HashMap<>();
        Map<LanguageTestType, String> languageRequirements = new HashMap<>();
    }
}
