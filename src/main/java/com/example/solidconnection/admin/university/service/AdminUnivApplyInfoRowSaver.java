package com.example.solidconnection.admin.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.COUNTRY_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.location.country.domain.Country;
import com.example.solidconnection.location.country.repository.CountryRepository;
import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.location.region.repository.RegionRepository;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.SemesterAvailableForDispatch;
import com.example.solidconnection.university.domain.TuitionFeeType;
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
    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String save(
            Map<String, String> rowData,
            Map<String, String> columnMappings,
            HomeUniversity homeUniversity,
            long termId
    ) {
        ImportData data = buildImportData(rowData, columnMappings);

        if (data.universityKoreanName == null || data.universityKoreanName.isBlank()) {
            throw new IllegalArgumentException("대학명(universityKoreanName) 컬럼이 매핑되지 않았습니다");
        }

        boolean existed = hostUniversityRepository.findByKoreanName(data.universityKoreanName).isPresent();
        HostUniversity hostUniversity = findOrCreateHostUniversity(data);
        String createdUniversityName = existed ? null : hostUniversity.getKoreanName();

        UnivApplyInfo univApplyInfo = new UnivApplyInfo(
                null,
                termId,
                homeUniversity,
                data.universityKoreanName,
                data.studentCapacity,
                data.tuitionFeeType,
                data.semesterAvailableForDispatch,
                data.semesterRequirement,
                data.detailsForLanguage,
                data.gpaRequirement,
                data.gpaRequirementCriteria,
                data.detailsForApply,
                data.detailsForMajor,
                data.detailsForAccommodation,
                data.detailsForEnglishCourse,
                data.details,
                data.extraInfo.isEmpty() ? null : data.extraInfo,
                new HashSet<>(),
                hostUniversity
        );

        UnivApplyInfo saved = univApplyInfoRepository.save(univApplyInfo);

        data.languageRequirements.forEach((testType, minScore) -> {
            LanguageRequirement lr = new LanguageRequirement(null, testType, minScore, saved);
            saved.addLanguageRequirements(lr);
        });

        return createdUniversityName;
    }

    private HostUniversity findOrCreateHostUniversity(ImportData data) {
        return hostUniversityRepository.findByKoreanName(data.universityKoreanName)
                .orElseGet(() -> createHostUniversity(data));
    }

    private HostUniversity createHostUniversity(ImportData data) {
        if (data.countryCode == null || data.countryCode.isBlank()) {
            throw new IllegalArgumentException(
                    "대학 '" + data.universityKoreanName + "'이(가) 존재하지 않습니다. 신규 대학 생성을 위해 국가코드(countryCode) 컬럼을 매핑해 주세요.");
        }

        Country country = countryRepository.findByCode(data.countryCode)
                .orElseThrow(() -> new CustomException(COUNTRY_NOT_FOUND));
        Region region = regionRepository.findById(country.getRegionCode()).orElse(null);

        return hostUniversityRepository.save(new HostUniversity(
                null,
                data.universityKoreanName,
                data.englishName != null ? data.englishName : "",
                data.formatName != null ? data.formatName : "",
                data.homepageUrl,
                data.englishCourseUrl,
                data.accommodationUrl,
                "",
                "",
                data.detailsForLocal,
                country,
                region
        ));
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
                case "englishName" -> data.englishName = value;
                case "formatName" -> data.formatName = value;
                case "countryCode" -> data.countryCode = value;
                case "homepageUrl" -> data.homepageUrl = value;
                case "englishCourseUrl" -> data.englishCourseUrl = value;
                case "accommodationUrl" -> data.accommodationUrl = value;
                case "detailsForLocal" -> data.detailsForLocal = value;
                case "studentCapacity" -> data.studentCapacity = Integer.parseInt(value);
                case "tuitionFeeType" -> data.tuitionFeeType = TuitionFeeType.valueOf(value);
                case "semesterAvailableForDispatch" -> data.semesterAvailableForDispatch = SemesterAvailableForDispatch.valueOf(value);
                case "semesterRequirement" -> data.semesterRequirement = value;
                case "detailsForLanguage" -> data.detailsForLanguage = value;
                case "gpaRequirement" -> data.gpaRequirement = value;
                case "gpaRequirementCriteria" -> data.gpaRequirementCriteria = value;
                case "detailsForApply" -> data.detailsForApply = value;
                case "detailsForMajor" -> data.detailsForMajor = value;
                case "detailsForAccommodation" -> data.detailsForAccommodation = value;
                case "detailsForEnglishCourse" -> data.detailsForEnglishCourse = value;
                case "details" -> data.details = value;
                default -> { return false; }
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static class ImportData {

        String universityKoreanName;
        String englishName;
        String formatName;
        String countryCode;
        String homepageUrl;
        String englishCourseUrl;
        String accommodationUrl;
        String detailsForLocal;
        Integer studentCapacity;
        TuitionFeeType tuitionFeeType;
        SemesterAvailableForDispatch semesterAvailableForDispatch;
        String semesterRequirement;
        String detailsForLanguage;
        String gpaRequirement;
        String gpaRequirementCriteria;
        String detailsForApply;
        String detailsForMajor;
        String detailsForAccommodation;
        String detailsForEnglishCourse;
        String details;
        Map<String, String> extraInfo = new HashMap<>();
        Map<LanguageTestType, String> languageRequirements = new HashMap<>();
    }
}
