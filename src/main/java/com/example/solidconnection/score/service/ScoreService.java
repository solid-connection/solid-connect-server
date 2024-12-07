package com.example.solidconnection.score.service;

import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.dto.GpaScoreRequest;
import com.example.solidconnection.score.dto.GpaScoreStatus;
import com.example.solidconnection.score.dto.GpaScoreStatusResponse;
import com.example.solidconnection.score.dto.LanguageTestScoreRequest;
import com.example.solidconnection.score.dto.LanguageTestScoreStatus;
import com.example.solidconnection.score.dto.LanguageTestScoreStatusResponse;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final GpaScoreRepository gpaScoreRepository;
    private final LanguageTestScoreRepository languageTestScoreRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional
    public Long submitGpaScore(String email, GpaScoreRequest gpaScoreRequest) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);

        GpaScore newGpaScore = new GpaScore(gpaScoreRequest.toGpa(), siteUser, gpaScoreRequest.issueDate());
        newGpaScore.setSiteUser(siteUser);
        GpaScore savedNewGpaScore = gpaScoreRepository.save(newGpaScore);  // 저장 후 반환된 객체
        return savedNewGpaScore.getId();  // 저장된 GPA Score의 ID 반환
    }

    @Transactional
    public Long submitLanguageTestScore(String email, LanguageTestScoreRequest languageTestScoreRequest) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        LanguageTest languageTest = languageTestScoreRequest.toLanguageTest();

        LanguageTestScore newScore = new LanguageTestScore(
                languageTest, languageTestScoreRequest.issueDate(), siteUser);
        newScore.setSiteUser(siteUser);
        LanguageTestScore savedNewScore = languageTestScoreRepository.save(newScore);  // 새로 저장한 객체
        return savedNewScore.getId();  // 저장된 객체의 ID 반환
    }

    @Transactional(readOnly = true)
    public GpaScoreStatusResponse getGpaScoreStatus(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        List<GpaScoreStatus> gpaScoreStatusList =
                Optional.ofNullable(siteUser.getGpaScoreList())
                        .map(scores -> scores.stream()
                                .map(GpaScoreStatus::from)
                                .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());
        return new GpaScoreStatusResponse(gpaScoreStatusList);
    }

    @Transactional(readOnly = true)
    public LanguageTestScoreStatusResponse getLanguageTestScoreStatus(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        List<LanguageTestScoreStatus> languageTestScoreStatusList =
                Optional.ofNullable(siteUser.getLanguageTestScoreList())
                        .map(scores -> scores.stream()
                                .map(LanguageTestScoreStatus::from)
                                .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());
        return new LanguageTestScoreStatusResponse(languageTestScoreStatusList);
    }
}
