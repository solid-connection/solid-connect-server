package com.example.solidconnection.admin.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.HOME_UNIVERSITY_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_INPUT;
import static com.example.solidconnection.common.exception.ErrorCode.TERM_NOT_FOUND;

import com.example.solidconnection.admin.university.dto.UnivApplyInfoFieldResponse;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoImportRequest;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoImportResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.util.MarkdownTableParser;
import com.example.solidconnection.term.repository.TermRepository;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.repository.HomeUniversityRepository;
import java.util.ArrayList;
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

    public UnivApplyInfoFieldResponse getFields() {
        return UnivApplyInfoFieldResponse.of();
    }

    @Transactional
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
}
