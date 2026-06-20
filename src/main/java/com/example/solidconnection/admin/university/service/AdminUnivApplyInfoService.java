package com.example.solidconnection.admin.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.HOME_UNIVERSITY_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_INPUT;
import static com.example.solidconnection.common.exception.ErrorCode.TERM_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.UNIV_APPLY_INFO_HAS_REFERENCES;
import static com.example.solidconnection.common.exception.ErrorCode.UNIV_APPLY_INFO_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_NOT_FOUND;

import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoCreateRequest;
import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoResponse;
import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoUpdateRequest;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoFieldResponse;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoImportRequest;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoImportResponse;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.cache.annotation.DefaultCacheOut;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.util.MarkdownTableParser;
import com.example.solidconnection.term.repository.TermRepository;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.repository.HomeUniversityRepository;
import com.example.solidconnection.university.repository.HostUniversityRepository;
import com.example.solidconnection.university.repository.LikedUnivApplyInfoRepository;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUnivApplyInfoService {

    private final TermRepository termRepository;
    private final HomeUniversityRepository homeUniversityRepository;
    private final MarkdownTableParser markdownTableParser;
    private final AdminUnivApplyInfoRowSaver rowSaver;
    private final UnivApplyInfoRepository univApplyInfoRepository;
    private final HostUniversityRepository hostUniversityRepository;
    private final LikedUnivApplyInfoRepository likedUnivApplyInfoRepository;
    private final ApplicationRepository applicationRepository;

    public UnivApplyInfoFieldResponse getFields() {
        return UnivApplyInfoFieldResponse.of();
    }

    @Transactional
    @DefaultCacheOut(
            key = {"univApplyInfoTextSearch", "university:recommend:general"},
            cacheManager = "customCacheManager",
            prefix = true
    )
    public UnivApplyInfoImportResponse importUnivApplyInfos(UnivApplyInfoImportRequest request) {
        validateColumnMappings(request.columnMappings());
        validateTermExists(request.termId());
        HomeUniversity homeUniversity = findHomeUniversity(request.homeUniversityId());

        List<Map<String, String>> rows = markdownTableParser.parse(request.markdown());

        List<String> createdUniversities = new ArrayList<>();

        for (Map<String, String> row : rows) {
            String createdName = rowSaver.save(row, request.columnMappings(), homeUniversity, request.termId());
            if (createdName != null) {
                createdUniversities.add(createdName);
            }
        }

        return new UnivApplyInfoImportResponse(rows.size(), createdUniversities);
    }

    private void validateColumnMappings(Map<String, String> columnMappings) {
        boolean hasBlankEntry = columnMappings.entrySet().stream()
                .anyMatch(e -> e.getKey().isBlank() || e.getValue().isBlank());
        if (hasBlankEntry) {
            throw new CustomException(INVALID_INPUT, "컬럼 매핑의 키와 값은 공백일 수 없습니다");
        }
    }

    private void validateTermExists(Long termId) {
        termRepository.findById(termId)
                .orElseThrow(() -> new CustomException(TERM_NOT_FOUND));
    }

    private HomeUniversity findHomeUniversity(Long homeUniversityId) {
        return homeUniversityRepository.findById(homeUniversityId)
                .orElseThrow(() -> new CustomException(HOME_UNIVERSITY_NOT_FOUND));
    }

    @Transactional
    @DefaultCacheOut(
            key = {"univApplyInfoTextSearch", "university:recommend:general"},
            cacheManager = "customCacheManager",
            prefix = true
    )
    public AdminUnivApplyInfoResponse createUnivApplyInfo(AdminUnivApplyInfoCreateRequest request) {
        validateTermExists(request.termId());
        HomeUniversity homeUniversity = findHomeUniversity(request.homeUniversityId());
        HostUniversity hostUniversity = findHostUniversity(request.hostUniversityId());

        UnivApplyInfo univApplyInfo = new UnivApplyInfo(
                null,
                request.termId(),
                homeUniversity,
                hostUniversity.getKoreanName(),
                request.studentCapacity(),
                request.semesterAvailableForDispatch(),
                request.semesterRequirement(),
                request.detailsForLanguage(),
                request.gpaRequirement(),
                request.gpaRequirementCriteria(),
                request.detailsForAccommodation(),
                request.extraInfo(),
                new HashSet<>(),
                hostUniversity
        );

        UnivApplyInfo saved = univApplyInfoRepository.save(univApplyInfo);

        if (request.languageRequirements() != null) {
            request.languageRequirements().forEach(lr -> {
                LanguageRequirement languageRequirement = new LanguageRequirement(
                        null, lr.languageTestType(), lr.minScore(), saved
                );
                saved.addLanguageRequirements(languageRequirement);
            });
        }

        return AdminUnivApplyInfoResponse.from(saved);
    }

    private HostUniversity findHostUniversity(Long hostUniversityId) {
        return hostUniversityRepository.findById(hostUniversityId)
                .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));
    }

    @Transactional
    @DefaultCacheOut(
            key = {"univApplyInfoTextSearch", "university:recommend:general"},
            cacheManager = "customCacheManager",
            prefix = true
    )
    public AdminUnivApplyInfoResponse updateUnivApplyInfo(long id, AdminUnivApplyInfoUpdateRequest request) {
        UnivApplyInfo univApplyInfo = univApplyInfoRepository.findById(id)
                .orElseThrow(() -> new CustomException(UNIV_APPLY_INFO_NOT_FOUND));

        univApplyInfo.update(
                request.studentCapacity(),
                request.semesterAvailableForDispatch(),
                request.semesterRequirement(),
                request.detailsForLanguage(),
                request.gpaRequirement(),
                request.gpaRequirementCriteria(),
                request.detailsForAccommodation(),
                request.extraInfo()
        );

        if (request.languageRequirements() != null) {
            univApplyInfo.clearLanguageRequirements();
            request.languageRequirements().forEach(lr -> {
                LanguageRequirement languageRequirement = new LanguageRequirement(
                        null, lr.languageTestType(), lr.minScore(), univApplyInfo
                );
                univApplyInfo.addLanguageRequirements(languageRequirement);
            });
        }

        return AdminUnivApplyInfoResponse.from(univApplyInfo);
    }

    @Transactional
    @DefaultCacheOut(
            key = {"univApplyInfoTextSearch", "university:recommend:general"},
            cacheManager = "customCacheManager",
            prefix = true
    )
    public void deleteUnivApplyInfo(long id) {
        UnivApplyInfo univApplyInfo = univApplyInfoRepository.findById(id)
                .orElseThrow(() -> new CustomException(UNIV_APPLY_INFO_NOT_FOUND));
        validateNoReferences(id);
        univApplyInfoRepository.delete(univApplyInfo);
    }

    private void validateNoReferences(long id) {
        if (likedUnivApplyInfoRepository.existsByUnivApplyInfoId(id)
                || applicationRepository.existsByChoicesUnivApplyInfoId(id)) {
            throw new CustomException(UNIV_APPLY_INFO_HAS_REFERENCES);
        }
    }

}
