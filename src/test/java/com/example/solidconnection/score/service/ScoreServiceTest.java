package com.example.solidconnection.score.service;

import com.example.solidconnection.application.domain.Gpa;
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
import java.util.List;

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
    void GPA_점수_상태를_조회한다() {
        // given
        SiteUser testUser = createSiteUser();
        List<GpaScore> scores = List.of(
                createGpaScore(testUser, 3.5, 4.5),
                createGpaScore(testUser, 3.8, 4.5)
        );

        // when
        GpaScoreStatusResponse response = scoreService.getGpaScoreStatus(testUser.getEmail());

        // then
        assertThat(response.gpaScoreStatusList())
                .hasSize(scores.size())
                .containsExactlyInAnyOrder(
                        scores.stream()
                                .map(GpaScoreStatus::from)
                                .toArray(GpaScoreStatus[]::new)
                );
    }

    @Test
    void GPA_점수가_없는_경우_빈_리스트를_반환한다() {
        // given
        SiteUser testUser = createSiteUser();

        // when
        GpaScoreStatusResponse response = scoreService.getGpaScoreStatus(testUser.getEmail());

        // then
        assertThat(response.gpaScoreStatusList()).isEmpty();
    }

    @Test
    void 어학_시험_점수_상태를_조회한다() {
        // given
        SiteUser testUser = createSiteUser();
        List<LanguageTestScore> scores = List.of(
                createLanguageTestScore(testUser, LanguageTestType.TOEIC, "100"),
                createLanguageTestScore(testUser, LanguageTestType.TOEFL_IBT, "7.5")
        );

        // when
        LanguageTestScoreStatusResponse response = scoreService.getLanguageTestScoreStatus(testUser.getEmail());

        // then
        assertThat(response.languageTestScoreStatusList())
                .hasSize(scores.size())
                .containsExactlyInAnyOrder(
                        scores.stream()
                                .map(LanguageTestScoreStatus::from)
                                .toArray(LanguageTestScoreStatus[]::new)
                );
    }

    @Test
    void 어학_시험_점수가_없는_경우_빈_리스트를_반환한다() {
        // given
        SiteUser testUser = createSiteUser();

        // when
        LanguageTestScoreStatusResponse response = scoreService.getLanguageTestScoreStatus(testUser.getEmail());

        // then
        assertThat(response.languageTestScoreStatusList()).isEmpty();
    }

    @Test
    void GPA_점수를_등록한다() {
        // given
        SiteUser testUser = createSiteUser();
        GpaScoreRequest request = createGpaScoreRequest();

        // when
        long scoreId = scoreService.submitGpaScore(testUser.getEmail(), request);
        GpaScore savedScore = gpaScoreRepository.findById(scoreId).orElseThrow();

        // then
        assertAll(
                () -> assertThat(savedScore.getId()).isEqualTo(scoreId),
                () -> assertThat(savedScore.getGpa().getGpa()).isEqualTo(request.gpa()),
                () -> assertThat(savedScore.getGpa().getGpaCriteria()).isEqualTo(request.gpaCriteria()),
                () -> assertThat(savedScore.getIssueDate()).isEqualTo(request.issueDate()),
                () -> assertThat(savedScore.getVerifyStatus()).isEqualTo(VerifyStatus.PENDING)
        );
    }

    @Test
    void 어학_시험_점수를_등록한다() {
        // given
        SiteUser testUser = createSiteUser();
        LanguageTestScoreRequest request = createLanguageTestScoreRequest();

        // when
        long scoreId = scoreService.submitLanguageTestScore(testUser.getEmail(), request);
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

    private GpaScore createGpaScore(SiteUser siteUser, double gpa, double gpaCriteria) {
        GpaScore gpaScore = new GpaScore(
                new Gpa(gpa, gpaCriteria, "/gpa-report.pdf"),
                siteUser,
                LocalDate.now()
        );
        return gpaScoreRepository.save(gpaScore);
    }

    private LanguageTestScore createLanguageTestScore(SiteUser siteUser, LanguageTestType languageTestType, String score) {
        LanguageTestScore languageTestScore = new LanguageTestScore(
                new LanguageTest(languageTestType, score, "/gpa-report.pdf"),
                LocalDate.now(),
                siteUser
        );
        return languageTestScoreRepository.save(languageTestScore);
    }

    private GpaScoreRequest createGpaScoreRequest() {
        return new GpaScoreRequest(
                3.5,
                4.5,
                LocalDate.now(),
                "/gpa-report.pdf"
        );
    }

    private LanguageTestScoreRequest createLanguageTestScoreRequest() {
        return new LanguageTestScoreRequest(
                LanguageTestType.TOEFL_IBT,
                "100",
                LocalDate.now(),
                "/gpa-report.pdf"
        );
    }
}
