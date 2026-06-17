package com.example.solidconnection.admin.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.COUNTRY_NOT_FOUND;

import com.example.solidconnection.admin.university.dto.UnivApplyInfoImportResponse.CellError;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
        validateImportData(data, rowData, columnMappings);

        boolean existed = hostUniversityRepository.findByKoreanName(data.universityKoreanName).isPresent();
        HostUniversity hostUniversity = findOrCreateHostUniversity(data, rowData, columnMappings);
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

    private void validateImportData(
            ImportData data,
            Map<String, String> rowData,
            Map<String, String> columnMappings
    ) {
        List<CellError> errors = new ArrayList<>();
        boolean universityKoreanNameBlank = data.universityKoreanName == null || data.universityKoreanName.isBlank();

        if (universityKoreanNameBlank) {
            errors.add(cellError(
                    rowData,
                    columnMappings,
                    "universityKoreanName",
                    "REQUIRED",
                    "대학명(universityKoreanName) 컬럼이 매핑되지 않았습니다"
            ));
        }

        if (universityKoreanNameBlank) {
            validateCountryCodeIfPresent(data, rowData, columnMappings, errors);
            throwIfErrors(errors);
        }

        boolean universityExists = hostUniversityRepository.findByKoreanName(data.universityKoreanName).isPresent();
        if (!universityExists && (data.countryCode == null || data.countryCode.isBlank())) {
            errors.add(cellError(
                    rowData,
                    columnMappings,
                    "universityCountryCode",
                    "REQUIRED",
                    "대학 '" + data.universityKoreanName + "'이(가) 존재하지 않습니다. 신규 대학 생성을 위해 국가코드(countryCode) 컬럼을 매핑해 주세요."
            ));
        }
        if (!universityExists) {
            validateCountryCodeIfPresent(data, rowData, columnMappings, errors);
        }

        throwIfErrors(errors);
    }

    private void validateCountryCodeIfPresent(
            ImportData data,
            Map<String, String> rowData,
            Map<String, String> columnMappings,
            List<CellError> errors
    ) {
        if (data.countryCode == null || data.countryCode.isBlank()) {
            return;
        }
        if (countryRepository.findByCode(data.countryCode).isPresent()) {
            return;
        }

        errors.add(cellError(
                rowData,
                columnMappings,
                "universityCountryCode",
                "NOT_FOUND",
                COUNTRY_NOT_FOUND.getMessage()
        ));
    }

    private void throwIfErrors(List<CellError> errors) {
        if (errors.isEmpty()) {
            return;
        }

        String message = errors.size() == 1 ? errors.get(0).message() : errors.size() + "개 컬럼에 문제가 있습니다.";
        throw new UnivApplyInfoImportFailureException(message, errors);
    }

    private HostUniversity findOrCreateHostUniversity(
            ImportData data,
            Map<String, String> rowData,
            Map<String, String> columnMappings
    ) {
        return hostUniversityRepository.findByKoreanName(data.universityKoreanName)
                .orElseGet(() -> createHostUniversity(data, rowData, columnMappings));
    }

    private HostUniversity createHostUniversity(
            ImportData data,
            Map<String, String> rowData,
            Map<String, String> columnMappings
    ) {
        if (data.countryCode == null || data.countryCode.isBlank()) {
            throwFailure(
                    rowData,
                    columnMappings,
                    "universityCountryCode",
                    "REQUIRED",
                    "대학 '" + data.universityKoreanName + "'이(가) 존재하지 않습니다. 신규 대학 생성을 위해 국가코드(countryCode) 컬럼을 매핑해 주세요."
            );
        }

        Country country = countryRepository.findByCode(data.countryCode)
                .orElseThrow(() -> new UnivApplyInfoImportFailureException(
                        COUNTRY_NOT_FOUND.getMessage(),
                        cellError(rowData, columnMappings, "universityCountryCode", "NOT_FOUND", COUNTRY_NOT_FOUND.getMessage())
                ));
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

    private void throwFailure(
            Map<String, String> rowData,
            Map<String, String> columnMappings,
            String field,
            String code,
            String message
    ) {
        throw new UnivApplyInfoImportFailureException(message, cellError(rowData, columnMappings, field, code, message));
    }

    private CellError cellError(
            Map<String, String> rowData,
            Map<String, String> columnMappings,
            String field,
            String code,
            String message
    ) {
        String header = findHeader(columnMappings, field);
        String value = header == null ? null : rowData.get(header);
        return new CellError(header, field, value, code, message);
    }

    private String findHeader(Map<String, String> columnMappings, String field) {
        return columnMappings.entrySet().stream()
                .filter(entry -> field.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
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
                case "universityEnglishName" -> data.englishName = value;
                case "universityFormatName" -> data.formatName = value;
                case "universityCountryCode" -> data.countryCode = value;
                case "universityHomepageUrl" -> data.homepageUrl = value;
                case "universityEnglishCourseUrl" -> data.englishCourseUrl = value;
                case "universityAccommodationUrl" -> data.accommodationUrl = value;
                case "universityDetailsForLocal" -> data.detailsForLocal = value;
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
