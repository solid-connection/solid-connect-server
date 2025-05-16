package com.example.solidconnection.admin.service;

import com.example.solidconnection.admin.dto.LanguageTestScoreResponse;
import com.example.solidconnection.admin.dto.LanguageTestScoreSearchResponse;
import com.example.solidconnection.admin.dto.LanguageTestScoreUpdateRequest;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
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

import static com.example.solidconnection.custom.exception.ErrorCode.LANGUAGE_TEST_SCORE_NOT_FOUND;
import static com.example.solidconnection.university.domain.LanguageTestType.TOEIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("어학 검증 관리자 서비스 테스트")
class AdminLanguageTestScoreServiceTest extends BaseIntegrationTest {

    @Autowired
    private AdminLanguageTestScoreService adminLanguageTestScoreService;

    @Autowired
    private LanguageTestScoreRepository languageTestScoreRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    private LanguageTestScore languageTestScore1;
    private LanguageTestScore languageTestScore2;
    private LanguageTestScore languageTestScore3;

    @BeforeEach
    void setUp() {
        SiteUser user1 = siteUserFixture.사용자(1, "test1");
        SiteUser user2 = siteUserFixture.사용자(2, "test2");
        SiteUser user3 = siteUserFixture.사용자(3, "test3");
        languageTestScore3 = createLanguageTestScore(user3, VerifyStatus.REJECTED);
        languageTestScore2 = createLanguageTestScore(user2, VerifyStatus.PENDING);
        languageTestScore1 = createLanguageTestScore(user1, VerifyStatus.PENDING);
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

    @Nested
    class 어학점수_검증_및_수정 {

        @Test
        void 어학점수와_검증상태를_정상적으로_수정한다() {
            // given
            LanguageTestScoreUpdateRequest request = new LanguageTestScoreUpdateRequest(
                    TOEIC,
                    "850",
                    VerifyStatus.APPROVED,
                    null
            );

            // when
            LanguageTestScoreResponse response = adminLanguageTestScoreService.updateLanguageTestScore(languageTestScore1.getId(), request);

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(languageTestScore1.getId()),
                    () -> assertThat(response.languageTestType()).isEqualTo(request.languageTestType()),
                    () -> assertThat(response.languageTestScore()).isEqualTo(request.languageTestScore()),
                    () -> assertThat(response.verifyStatus()).isEqualTo(request.verifyStatus()),
                    () -> assertThat(response.rejectedReason()).isNull()
            );
        }

        @Test
        void 승인상태로_변경_시_거절사유가_입력되어도_null로_저장된다() {
            // given
            LanguageTestScoreUpdateRequest request = new LanguageTestScoreUpdateRequest(
                    TOEIC,
                    "850",
                    VerifyStatus.APPROVED,
                    "이 거절사유는 무시되어야 함"
            );

            // when
            LanguageTestScoreResponse response = adminLanguageTestScoreService.updateLanguageTestScore(languageTestScore1.getId(), request);

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(languageTestScore1.getId()),
                    () -> assertThat(response.languageTestType()).isEqualTo(request.languageTestType()),
                    () -> assertThat(response.languageTestScore()).isEqualTo(request.languageTestScore()),
                    () -> assertThat(response.verifyStatus()).isEqualTo(VerifyStatus.APPROVED),
                    () -> assertThat(response.rejectedReason()).isNull()
            );
        }

        @Test
        void 존재하지_않는_어학점수_수정_시_예외_응답을_반환한다() {
            // given
            long invalidLanguageTestScoreId = 9999L;
            LanguageTestScoreUpdateRequest request = new LanguageTestScoreUpdateRequest(
                    TOEIC,
                    "850",
                    VerifyStatus.APPROVED,
                    null
            );

            // when & then
            assertThatCode(() -> adminLanguageTestScoreService.updateLanguageTestScore(invalidLanguageTestScoreId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(LANGUAGE_TEST_SCORE_NOT_FOUND.getMessage());
        }
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
