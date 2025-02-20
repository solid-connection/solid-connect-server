package com.example.solidconnection.admin.service;

import com.example.solidconnection.admin.dto.LanguageTestScoreSearchResponse;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.type.VerifyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static com.example.solidconnection.type.LanguageTestType.TOEIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("어학 검증 관리자 서비스 테스트")
class AdminLanguageTestScoreServiceTest extends BaseIntegrationTest {

    @Autowired
    private AdminLanguageTestScoreService adminLanguageTestScoreService;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private LanguageTestScoreRepository languageTestScoreRepository;

    private SiteUser siteUser1;
    private SiteUser siteUser2;
    private SiteUser siteUser3;
    private LanguageTestScore languageTestScore1;
    private LanguageTestScore languageTestScore2;
    private LanguageTestScore languageTestScore3;

    @BeforeEach
    void setUp() {
        siteUser1 = createSiteUser(1, "test1");
        siteUser2 = createSiteUser(2, "test2");
        siteUser3 = createSiteUser(3, "test3");
        languageTestScore3 = createLanguageTestScore(siteUser3, VerifyStatus.REJECTED);
        languageTestScore2 = createLanguageTestScore(siteUser2, VerifyStatus.PENDING);
        languageTestScore1 = createLanguageTestScore(siteUser1, VerifyStatus.PENDING);
    }

    @Nested
    class 지원한_어학_목록_조회 {

        @Test
        void 검증_상태를_조건으로_페이징하여_조회한다() {
            // given
            ScoreSearchCondition condition = new ScoreSearchCondition(VerifyStatus.PENDING, null, null);
            Pageable pageable = PageRequest.of(0, 10);
            List<LanguageTestScore> expectedLanguageTestScores = List.of(languageTestScore1, languageTestScore2);

            // when
            Page<LanguageTestScoreSearchResponse> response = adminLanguageTestScoreService.searchLanguageTestScores(condition, pageable);

            // then
            assertThat(response.getContent())
                    .hasSize(expectedLanguageTestScores.size())
                    .zipSatisfy(expectedLanguageTestScores, (actual, expected) -> assertAll(
                            () -> assertThat(actual.languageTestScoreStatusResponse().id()).isEqualTo(expected.getId()),
                            () -> assertThat(actual.languageTestScoreStatusResponse().languageTestResponse().languageTestType())
                                    .isEqualTo(expected.getLanguageTest().getLanguageTestType()),
                            () -> assertThat(actual.languageTestScoreStatusResponse().languageTestResponse().languageTestScore())
                                    .isEqualTo(expected.getLanguageTest().getLanguageTestScore()),
                            () -> assertThat(actual.languageTestScoreStatusResponse().languageTestResponse().languageTestReportUrl())
                                    .isEqualTo(expected.getLanguageTest().getLanguageTestReportUrl()),
                            () -> assertThat(actual.languageTestScoreStatusResponse().verifyStatus()).isEqualTo(expected.getVerifyStatus()),

                            () -> assertThat(actual.siteUserResponse().id()).isEqualTo(expected.getSiteUser().getId()),
                            () -> assertThat(actual.siteUserResponse().profileImageUrl()).isEqualTo(expected.getSiteUser().getProfileImageUrl()),
                            () -> assertThat(actual.siteUserResponse().nickname()).isEqualTo(expected.getSiteUser().getNickname())
                    ));
        }

        @Test
        void 닉네임으로_페이징하여_조회한다() {
            // given
            ScoreSearchCondition condition = new ScoreSearchCondition(null, "test", null);
            Pageable pageable = PageRequest.of(0, 10);
            List<LanguageTestScore> expectedLanguageTestScores = List.of(languageTestScore1, languageTestScore2, languageTestScore3);

            // when
            Page<LanguageTestScoreSearchResponse> response = adminLanguageTestScoreService.searchLanguageTestScores(condition, pageable);

            // then
            assertThat(response.getContent())
                    .hasSize(expectedLanguageTestScores.size())
                    .zipSatisfy(expectedLanguageTestScores, (actual, expected) -> assertAll(
                            () -> assertThat(actual.languageTestScoreStatusResponse().id()).isEqualTo(expected.getId()),
                            () -> assertThat(actual.languageTestScoreStatusResponse().languageTestResponse().languageTestType())
                                    .isEqualTo(expected.getLanguageTest().getLanguageTestType()),
                            () -> assertThat(actual.languageTestScoreStatusResponse().languageTestResponse().languageTestScore())
                                    .isEqualTo(expected.getLanguageTest().getLanguageTestScore()),
                            () -> assertThat(actual.languageTestScoreStatusResponse().languageTestResponse().languageTestReportUrl())
                                    .isEqualTo(expected.getLanguageTest().getLanguageTestReportUrl()),
                            () -> assertThat(actual.languageTestScoreStatusResponse().verifyStatus()).isEqualTo(expected.getVerifyStatus()),

                            () -> assertThat(actual.siteUserResponse().id()).isEqualTo(expected.getSiteUser().getId()),
                            () -> assertThat(actual.siteUserResponse().profileImageUrl()).isEqualTo(expected.getSiteUser().getProfileImageUrl()),
                            () -> assertThat(actual.siteUserResponse().nickname()).isEqualTo(expected.getSiteUser().getNickname())
                    ));
        }

        @Test
        void 모든_조건으로_페이징하여_조회한다() {
            // given
            ScoreSearchCondition condition = new ScoreSearchCondition(VerifyStatus.PENDING, "test1", LocalDate.now());
            Pageable pageable = PageRequest.of(0, 10);
            List<LanguageTestScore> expectedLanguageTestScores = List.of(languageTestScore1);

            // when
            Page<LanguageTestScoreSearchResponse> response = adminLanguageTestScoreService.searchLanguageTestScores(condition, pageable);

            // then
            assertThat(response.getContent())
                    .hasSize(expectedLanguageTestScores.size())
                    .zipSatisfy(expectedLanguageTestScores, (actual, expected) -> assertAll(
                            () -> assertThat(actual.languageTestScoreStatusResponse().id()).isEqualTo(expected.getId()),
                            () -> assertThat(actual.languageTestScoreStatusResponse().languageTestResponse().languageTestType())
                                    .isEqualTo(expected.getLanguageTest().getLanguageTestType()),
                            () -> assertThat(actual.languageTestScoreStatusResponse().languageTestResponse().languageTestScore())
                                    .isEqualTo(expected.getLanguageTest().getLanguageTestScore()),
                            () -> assertThat(actual.languageTestScoreStatusResponse().languageTestResponse().languageTestReportUrl())
                                    .isEqualTo(expected.getLanguageTest().getLanguageTestReportUrl()),
                            () -> assertThat(actual.languageTestScoreStatusResponse().verifyStatus()).isEqualTo(expected.getVerifyStatus()),

                            () -> assertThat(actual.siteUserResponse().id()).isEqualTo(expected.getSiteUser().getId()),
                            () -> assertThat(actual.siteUserResponse().profileImageUrl()).isEqualTo(expected.getSiteUser().getProfileImageUrl()),
                            () -> assertThat(actual.siteUserResponse().nickname()).isEqualTo(expected.getSiteUser().getNickname())
                    ));
        }
    }

    private SiteUser createSiteUser(int index, String nickname) {
        SiteUser siteUser = new SiteUser(
                "test" + index + " @example.com",
                nickname,
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
        return siteUserRepository.save(siteUser);
    }

    private LanguageTestScore createLanguageTestScore(SiteUser siteUser, VerifyStatus status) {
        LanguageTestScore languageTestScore = new LanguageTestScore(
                new LanguageTest(TOEIC, "500", "/toeic-report.pdf"),
                siteUser
        );
        languageTestScore.setVerifyStatus(status);
        return languageTestScoreRepository.save(languageTestScore);
    }
}
