package com.example.solidconnection.score.service;

import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.dto.GpaScoreRequest;
import com.example.solidconnection.score.dto.LanguageTestScoreRequest;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.type.VerifyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("점수 서비스 테스트")
class ScoreServiceTest extends BaseIntegrationTest {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private GpaScoreRepository gpaScoreRepository;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private LanguageTestScoreRepository languageTestScoreRepository;

    @Test
    void GPA_점수를_등록한다() {
        // given
        SiteUser testUser = createSiteUser();
        GpaScoreRequest request = createGpaScoreRequest();

        // when
        Long scoreId = scoreService.submitGpaScore(testUser.getEmail(), request);
        GpaScore savedScore = gpaScoreRepository.findById(scoreId).orElseThrow();

        // then
        assertAll(
                () -> assertThat(savedScore.getId()).isEqualTo(scoreId),
                () -> assertThat(savedScore.getGpa().getGpa()).isEqualTo(request.gpa()),
                () -> assertThat(savedScore.getGpa().getGpaCriteria()).isEqualTo(request.gpaCriteria()),
                () -> assertThat(savedScore.getIssueDate()).isEqualTo(request.issueDate())
        );
    }

    @Test
    void 어학_시험_점수를_등록한다() {
        // given
        SiteUser testUser = createSiteUser();
        LanguageTestScoreRequest request = createLanguageTestScoreRequest();

        // when
        Long scoreId = scoreService.submitLanguageTestScore(testUser.getEmail(), request);
        LanguageTestScore savedScore = languageTestScoreRepository.findById(scoreId).orElseThrow();

        // then
        assertAll(
                () -> assertThat(savedScore.getId()).isEqualTo(scoreId),
                () -> assertThat(savedScore.getLanguageTest().getLanguageTestType()).isEqualTo(request.languageTestType()),
                () -> assertThat(savedScore.getLanguageTest().getLanguageTestScore()).isEqualTo(request.languageTestScore()),
                () -> assertThat(savedScore.getIssueDate()).isEqualTo(request.issueDate()),
                () -> assertThat(savedScore.getVerifyStatus()).isEqualTo(VerifyStatus.PENDING)
        );
    }

    private SiteUser createSiteUser() {
        SiteUser siteUser = new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
        return siteUserRepository.save(siteUser);
    }

    private GpaScoreRequest createGpaScoreRequest() {
        return new GpaScoreRequest(
                3.5,
                4.5,
                LocalDate.now(),
                "https://example.com/gpa-report.pdf"
        );
    }

    private LanguageTestScoreRequest createLanguageTestScoreRequest() {
        return new LanguageTestScoreRequest(
                LanguageTestType.TOEFL_IBT,
                "100",
                LocalDate.now(),
                "https://example.com/gpa-report.pdf"
        );
    }
}
