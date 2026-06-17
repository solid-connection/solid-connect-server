package com.example.solidconnection.admin.term.service;

import static com.example.solidconnection.common.exception.ErrorCode.TERM_ALREADY_EXISTS;
import static com.example.solidconnection.common.exception.ErrorCode.TERM_NOT_FOUND;

import com.example.solidconnection.admin.term.dto.AdminTermCreateRequest;
import com.example.solidconnection.admin.term.dto.AdminTermResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.repository.TermRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminTermService {

    private final TermRepository termRepository;

    @Transactional(readOnly = true)
    public List<AdminTermResponse> getAllTerms() {
        return termRepository.findAll()
                .stream()
                .map(AdminTermResponse::from)
                .toList();
    }

    @Transactional
    public AdminTermResponse createTerm(AdminTermCreateRequest request) {
        termRepository.findByName(request.name())
                .ifPresent(t -> {
                    throw new CustomException(TERM_ALREADY_EXISTS);
                });
        Term saved = termRepository.save(new Term(request.name(), false));
        return AdminTermResponse.from(saved);
    }

    @Transactional
    public AdminTermResponse activateTerm(Long id) {
        Term termToActivate = termRepository.findById(id)
                .orElseThrow(() -> new CustomException(TERM_NOT_FOUND));
        if (!Boolean.TRUE.equals(termToActivate.getIsCurrent())) {
            termRepository.deactivateCurrentTerm();
        }
        termToActivate.activate();
        return AdminTermResponse.from(termToActivate);
    }
}
