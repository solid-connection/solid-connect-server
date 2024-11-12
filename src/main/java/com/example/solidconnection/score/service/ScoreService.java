package com.example.solidconnection.score.service;

import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.dto.*;
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

        Optional<GpaScore> gpaScoreBySiteUser = gpaScoreRepository.findGpaScoreBySiteUser(siteUser);
        if (gpaScoreBySiteUser.isPresent()) {
            GpaScore gpaScore = gpaScoreBySiteUser.get();
            gpaScore.update(gpaScoreRequest.toGpa(), gpaScoreRequest.issueDate());
            GpaScore savedGpaScore = gpaScoreRepository.save(gpaScore);  // 저장 후 반환된 객체
            return savedGpaScore.getId();  // 저장된 GPA Score의 ID 반환
        } else {
            GpaScore newGpaScore = new GpaScore(gpaScoreRequest.toGpa(), siteUser, gpaScoreRequest.issueDate());
            newGpaScore.setSiteUser(siteUser);
            GpaScore savedNewGpaScore = gpaScoreRepository.save(newGpaScore);  // 저장 후 반환된 객체
            return savedNewGpaScore.getId();  // 저장된 GPA Score의 ID 반환
        }
    }

    @Transactional
    public Long submitLanguageTestScore(String email, LanguageTestScoreRequest languageTestScoreRequest) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        LanguageTest languageTest = languageTestScoreRequest.toLanguageTest();

        Optional<LanguageTestScore> languageTestScore =
                languageTestScoreRepository.findLanguageTestScoreBySiteUserAndLanguageTest_LanguageTestType(siteUser, languageTest.getLanguageTestType());

        if (languageTestScore.isPresent()) {
            // 기존 이력이 있을 경우 업데이트
            LanguageTestScore existingScore = languageTestScore.get();
            existingScore.update(languageTest, languageTestScoreRequest.issueDate());
            languageTestScoreRepository.save(existingScore);
            return existingScore.getId();  // 업데이트된 객체의 ID 반환
        } else {
            // 기존 이력이 없을 경우 새로 생성
            LanguageTestScore newScore = new LanguageTestScore(
                    languageTest, languageTestScoreRequest.issueDate(), siteUser);
            newScore.setSiteUser(siteUser);
            LanguageTestScore savedNewScore = languageTestScoreRepository.save(newScore);  // 새로 저장한 객체
            return savedNewScore.getId();  // 저장된 객체의 ID 반환
        }
    }

    @Transactional(readOnly = true)
    public GpaScoreStatusResponse getGpaScoreStatus(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        GpaScore gpaScore = siteUser.getGpaScore();
        if (gpaScore == null) {
            return null;
        }
        return GpaScoreStatusResponse.from(gpaScore);
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
