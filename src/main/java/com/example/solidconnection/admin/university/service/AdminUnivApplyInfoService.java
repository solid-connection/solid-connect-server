package com.example.solidconnection.admin.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.HOME_UNIVERSITY_NOT_FOUND;
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
        validateTermExists(request.termId());
        HomeUniversity homeUniversity = findHomeUniversity(request.homeUniversityId());

        List<Map<String, String>> rows = markdownTableParser.parse(request.markdown());

        int successCount = 0;
        List<String> createdUniversities = new ArrayList<>();

        for (Map<String, String> row : rows) {
            try {
                String createdName = rowSaver.save(row, request.columnMappings(), homeUniversity, request.termId());
                successCount++;
                if (createdName != null) {
                    createdUniversities.add(createdName);
                }
            } catch (Exception e) {
                // row failed, skip
            }
        }

        return new UnivApplyInfoImportResponse(successCount, createdUniversities);
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
