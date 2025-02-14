package com.example.solidconnection.admin.service;

import com.example.solidconnection.admin.dto.GpaScoreSearchResponse;
import com.example.solidconnection.admin.dto.GpaScoreVerificationResponse;
import com.example.solidconnection.admin.dto.GpaScoreVerifyRequest;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.repository.GpaScoreRepository;
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

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_GPA_SCORE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("성적 검증 관리자 서비스 테스트")
class ScoreVerificationAdminServiceTest extends BaseIntegrationTest {

    @Autowired
    private ScoreVerificationAdminService scoreVerificationAdminService;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private GpaScoreRepository gpaScoreRepository;

    private SiteUser siteUser1;
    private SiteUser siteUser2;
    private SiteUser siteUser3;
    private GpaScore gpaScore1;
    private GpaScore gpaScore2;
    private GpaScore gpaScore3;

    @BeforeEach
    void setUp() {
        siteUser1 = createSiteUser(1, "test1");
        siteUser2 = createSiteUser(2, "test2");
        siteUser3 = createSiteUser(3, "test3");
        gpaScore3 = createGpaScore(siteUser3, VerifyStatus.REJECTED);
        gpaScore2 = createGpaScore(siteUser2, VerifyStatus.PENDING);
        gpaScore1 = createGpaScore(siteUser1, VerifyStatus.PENDING);
    }

    @Nested
    class 지원한_GPA_목록_조회 {

        @Test
        void 검증_상태를_조건으로_페이징하여_조회한다() {
            // given
            ScoreSearchCondition condition = new ScoreSearchCondition(VerifyStatus.PENDING, null, null);
            Pageable pageable = PageRequest.of(1, 10);
            List<GpaScore> expectedGpaScores = List.of(gpaScore1, gpaScore2);

            // when
            Page<GpaScoreSearchResponse> response = scoreVerificationAdminService.searchGpaScores(condition, pageable);

            // then
            assertThat(response.getContent())
                    .hasSize(expectedGpaScores.size())
                    .zipSatisfy(expectedGpaScores, (actual, expected) -> assertAll(
                            () -> assertThat(actual.gpaScoreStatus().id()).isEqualTo(expected.getId()),
                            () -> assertThat(actual.gpaScoreStatus().gpa().getGpa()).isEqualTo(expected.getGpa().getGpa()),
                            () -> assertThat(actual.gpaScoreStatus().gpa().getGpaCriteria()).isEqualTo(expected.getGpa().getGpaCriteria()),
                            () -> assertThat(actual.gpaScoreStatus().gpa().getGpaReportUrl()).isEqualTo(expected.getGpa().getGpaReportUrl()),
                            () -> assertThat(actual.gpaScoreStatus().verifyStatus()).isEqualTo(expected.getVerifyStatus()),

                            () -> assertThat(actual.siteUserDto().id()).isEqualTo(expected.getSiteUser().getId()),
                            () -> assertThat(actual.siteUserDto().profileImageUrl()).isEqualTo(expected.getSiteUser().getProfileImageUrl()),
                            () -> assertThat(actual.siteUserDto().nickname()).isEqualTo(expected.getSiteUser().getNickname())
                    ));
        }

        @Test
        void 닉네임으로_페이징하여_조회한다() {
            // given
            ScoreSearchCondition condition = new ScoreSearchCondition(null, "test", null);
            Pageable pageable = PageRequest.of(1, 10);
            List<GpaScore> expectedGpaScores = List.of(gpaScore1, gpaScore2, gpaScore3);

            // when
            Page<GpaScoreSearchResponse> response = scoreVerificationAdminService.searchGpaScores(condition, pageable);

            // then
            assertThat(response.getContent())
                    .hasSize(expectedGpaScores.size())
                    .zipSatisfy(expectedGpaScores, (actual, expected) -> assertAll(
                            () -> assertThat(actual.gpaScoreStatus().id()).isEqualTo(expected.getId()),
                            () -> assertThat(actual.gpaScoreStatus().gpa().getGpa()).isEqualTo(expected.getGpa().getGpa()),
                            () -> assertThat(actual.gpaScoreStatus().gpa().getGpaCriteria()).isEqualTo(expected.getGpa().getGpaCriteria()),
                            () -> assertThat(actual.gpaScoreStatus().gpa().getGpaReportUrl()).isEqualTo(expected.getGpa().getGpaReportUrl()),
                            () -> assertThat(actual.gpaScoreStatus().verifyStatus()).isEqualTo(expected.getVerifyStatus()),

                            () -> assertThat(actual.siteUserDto().id()).isEqualTo(expected.getSiteUser().getId()),
                            () -> assertThat(actual.siteUserDto().profileImageUrl()).isEqualTo(expected.getSiteUser().getProfileImageUrl()),
                            () -> assertThat(actual.siteUserDto().nickname()).isEqualTo(expected.getSiteUser().getNickname())
                    ));
        }

        @Test
        void 모든_조건으로_페이징하여_조회한다() {
            // given
            ScoreSearchCondition condition = new ScoreSearchCondition(VerifyStatus.PENDING, "test1", LocalDate.now());
            Pageable pageable = PageRequest.of(1, 10);
            List<GpaScore> expectedGpaScores = List.of(gpaScore1);

            // when
            Page<GpaScoreSearchResponse> response = scoreVerificationAdminService.searchGpaScores(condition, pageable);

            // then
            assertThat(response.getContent())
                    .hasSize(expectedGpaScores.size())
                    .zipSatisfy(expectedGpaScores, (actual, expected) -> assertAll(
                            () -> assertThat(actual.gpaScoreStatus().id()).isEqualTo(expected.getId()),
                            () -> assertThat(actual.gpaScoreStatus().gpa().getGpa()).isEqualTo(expected.getGpa().getGpa()),
                            () -> assertThat(actual.gpaScoreStatus().gpa().getGpaCriteria()).isEqualTo(expected.getGpa().getGpaCriteria()),
                            () -> assertThat(actual.gpaScoreStatus().gpa().getGpaReportUrl()).isEqualTo(expected.getGpa().getGpaReportUrl()),
                            () -> assertThat(actual.gpaScoreStatus().verifyStatus()).isEqualTo(expected.getVerifyStatus()),

                            () -> assertThat(actual.siteUserDto().id()).isEqualTo(expected.getSiteUser().getId()),
                            () -> assertThat(actual.siteUserDto().profileImageUrl()).isEqualTo(expected.getSiteUser().getProfileImageUrl()),
                            () -> assertThat(actual.siteUserDto().nickname()).isEqualTo(expected.getSiteUser().getNickname())
                    ));
        }
    }

    @Nested
    class GPA_점수_검증 {

        @Test
        void GPA_점수를_정상적으로_승인한다() {
            // given
            GpaScoreVerifyRequest request = new GpaScoreVerifyRequest(
                    VerifyStatus.APPROVED,
                    null
            );

            // when
            GpaScoreVerificationResponse response = scoreVerificationAdminService.verifyGpaScore(gpaScore1.getId(), request);

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(gpaScore1.getId()),
                    () -> assertThat(response.verifyStatus()).isEqualTo(VerifyStatus.APPROVED)
            );
        }

        @Test
        void GPA_점수를_정상적으로_거절한다() {
            // given
            GpaScoreVerifyRequest request = new GpaScoreVerifyRequest(
                    VerifyStatus.REJECTED,
                    "잘못된 성적입니다."
            );

            // when
            GpaScoreVerificationResponse response = scoreVerificationAdminService.verifyGpaScore(gpaScore1.getId(), request);

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(gpaScore1.getId()),
                    () -> assertThat(response.verifyStatus()).isEqualTo(VerifyStatus.REJECTED)
            );
        }

        @Test
        void 존재하지_않는_GPA_성적을_검증하면_예외_응답을_반환한다() {
            // given
            long invalidGpaScoreId = 9999L;
            GpaScoreVerifyRequest request = new GpaScoreVerifyRequest(
                    VerifyStatus.APPROVED,
                    null
            );

            // when & then
            assertThatCode(() -> scoreVerificationAdminService.verifyGpaScore(invalidGpaScoreId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_GPA_SCORE.getMessage());
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

    private GpaScore createGpaScore(SiteUser siteUser, VerifyStatus status) {
        GpaScore gpaScore = new GpaScore(
                new Gpa(4.0, 4.5, "/gpa-report.pdf"),
                siteUser
        );
        gpaScore.setVerifyStatus(status);
        return gpaScoreRepository.save(gpaScore);
    }
}
