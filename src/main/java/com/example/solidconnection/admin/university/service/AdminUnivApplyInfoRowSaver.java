package com.example.solidconnection.admin.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.COUNTRY_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_INPUT;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUnivApplyInfoRowSaver {

    private final HostUniversityRepository hostUniversityRepository;
    private final UnivApplyInfoRepository univApplyInfoRepository;
    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;

    @Transactional
    public String save(
            Map<String, String> rowData,
            Map<String, String> columnMappings,
            HomeUniversity homeUniversity,
            long termId
    ) {
        ImportData data = buildImportData(rowData, columnMappings);

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
            throw new CustomException(INVALID_INPUT,
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

        applyStructuredField(data, header, targetField, value);
    }

    private void applyStructuredField(ImportData data, String header, String fieldName, String value) {
        switch (fieldName) {
            case "universityKoreanName" -> applyWithLength(value, 100, s -> data.universityKoreanName = s);
            case "universityEnglishName" -> applyWithLength(value, 200, s -> data.englishName = s);
            case "universityFormatName" -> applyWithLength(value, 100, s -> data.formatName = s);
            case "universityCountryCode" -> data.countryCode = value;
            case "universityHomepageUrl" -> applyWithLength(value, 500, s -> data.homepageUrl = s);
            case "universityEnglishCourseUrl" -> applyWithLength(value, 500, s -> data.englishCourseUrl = s);
            case "universityAccommodationUrl" -> applyWithLength(value, 500, s -> data.accommodationUrl = s);
            case "universityDetailsForLocal" -> applyWithLength(value, 1000, s -> data.detailsForLocal = s);
            case "studentCapacity" -> {
                try {
                    data.studentCapacity = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new CustomException(INVALID_INPUT, "선발 인원은 정수여야 합니다: '" + value + "'");
                }
            }
            case "tuitionFeeType" -> {
                try {
                    data.tuitionFeeType = TuitionFeeType.valueOf(value);
                } catch (IllegalArgumentException e) {
                    throw new CustomException(INVALID_INPUT,
                            "유효하지 않은 등록금 유형입니다. 가능한 값: " + validEnumValues(TuitionFeeType.values()));
                }
            }
            case "semesterAvailableForDispatch" -> {
                try {
                    data.semesterAvailableForDispatch = SemesterAvailableForDispatch.valueOf(value);
                } catch (IllegalArgumentException e) {
                    throw new CustomException(INVALID_INPUT,
                            "유효하지 않은 파견 가능 학기입니다. 가능한 값: " + validEnumValues(SemesterAvailableForDispatch.values()));
                }
            }
            case "semesterRequirement" -> applyWithLength(value, 100, s -> data.semesterRequirement = s);
            case "detailsForLanguage" -> applyWithLength(value, 2000, s -> data.detailsForLanguage = s);
            case "gpaRequirement" -> applyWithLength(value, 100, s -> data.gpaRequirement = s);
            case "gpaRequirementCriteria" -> applyWithLength(value, 100, s -> data.gpaRequirementCriteria = s);
            case "detailsForApply" -> applyWithLength(value, 3000, s -> data.detailsForApply = s);
            case "detailsForMajor" -> applyWithLength(value, 3000, s -> data.detailsForMajor = s);
            case "detailsForAccommodation" -> applyWithLength(value, 2000, s -> data.detailsForAccommodation = s);
            case "detailsForEnglishCourse" -> applyWithLength(value, 1000, s -> data.detailsForEnglishCourse = s);
            case "details" -> applyWithLength(value, 3000, s -> data.details = s);
            default -> data.extraInfo.put(header, value);
        }
    }

    private void applyWithLength(String value, int maxLength, Consumer<String> setter) {
        if (value.length() > maxLength) {
            throw new CustomException(INVALID_INPUT,
                    "값이 최대 길이(" + maxLength + "자)를 초과했습니다: " + value.length() + "자");
        }
        setter.accept(value);
    }

    private String validEnumValues(Enum<?>[] values) {
        return Arrays.stream(values)
                .map(Enum::name)
                .collect(Collectors.joining(", "));
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
