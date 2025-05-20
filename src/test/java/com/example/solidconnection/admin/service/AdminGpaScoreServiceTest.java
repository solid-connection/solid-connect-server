package com.example.solidconnection.admin.service;

import com.example.solidconnection.admin.dto.GpaScoreResponse;
import com.example.solidconnection.admin.dto.GpaScoreSearchResponse;
import com.example.solidconnection.admin.dto.GpaScoreUpdateRequest;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.application.domain.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.fixture.GpaScoreFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
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

import static com.example.solidconnection.common.exception.ErrorCode.GPA_SCORE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
@DisplayName("학점 검증 관리자 서비스 테스트")
class AdminGpaScoreServiceTest {

    @Autowired
    private AdminGpaScoreService adminGpaScoreService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private GpaScoreFixture gpaScoreFixture;

    private GpaScore gpaScore1;
    private GpaScore gpaScore2;
    private GpaScore gpaScore3;

    @BeforeEach
    void setUp() {
        SiteUser user1 = siteUserFixture.사용자(1, "test1");
        SiteUser user2 = siteUserFixture.사용자(2, "test2");
        SiteUser user3 = siteUserFixture.사용자(3, "test3");
        gpaScore3 = gpaScoreFixture.GPA점수(3.5, 4.5, VerifyStatus.REJECTED, user3);
        gpaScore2 = gpaScoreFixture.GPA점수(3.5, 4.5, VerifyStatus.PENDING, user2);
        gpaScore1 = gpaScoreFixture.GPA점수(3.5, 4.5, VerifyStatus.PENDING, user1);
    }

    @Nested
    class 지원한_GPA_목록_조회 {

        @Test
        void 검증_상태를_조건으로_페이징하여_조회한다() {
            // given
            ScoreSearchCondition condition = new ScoreSearchCondition(VerifyStatus.PENDING, null, null);
            Pageable pageable = PageRequest.of(0, 10);
            List<GpaScore> expectedGpaScores = List.of(gpaScore1, gpaScore2);

            // when
            Page<GpaScoreSearchResponse> response = adminGpaScoreService.searchGpaScores(condition, pageable);

            // then
            assertThat(response.getContent())
                    .hasSize(expectedGpaScores.size())
                    .zipSatisfy(expectedGpaScores, (actual, expected) -> assertAll(
                            () -> assertThat(actual.gpaScoreStatusResponse().id()).isEqualTo(expected.getId()),
                            () -> assertThat(actual.gpaScoreStatusResponse().gpaResponse().gpa()).isEqualTo(expected.getGpa().getGpa()),
                            () -> assertThat(actual.gpaScoreStatusResponse().gpaResponse().gpaCriteria()).isEqualTo(expected.getGpa().getGpaCriteria()),
                            () -> assertThat(actual.gpaScoreStatusResponse().gpaResponse().gpaReportUrl()).isEqualTo(expected.getGpa().getGpaReportUrl()),
                            () -> assertThat(actual.gpaScoreStatusResponse().verifyStatus()).isEqualTo(expected.getVerifyStatus()),

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
            List<GpaScore> expectedGpaScores = List.of(gpaScore1, gpaScore2, gpaScore3);

            // when
            Page<GpaScoreSearchResponse> response = adminGpaScoreService.searchGpaScores(condition, pageable);

            // then
            assertThat(response.getContent())
                    .hasSize(expectedGpaScores.size())
                    .zipSatisfy(expectedGpaScores, (actual, expected) -> assertAll(
                            () -> assertThat(actual.gpaScoreStatusResponse().id()).isEqualTo(expected.getId()),
                            () -> assertThat(actual.gpaScoreStatusResponse().gpaResponse().gpa()).isEqualTo(expected.getGpa().getGpa()),
                            () -> assertThat(actual.gpaScoreStatusResponse().gpaResponse().gpaCriteria()).isEqualTo(expected.getGpa().getGpaCriteria()),
                            () -> assertThat(actual.gpaScoreStatusResponse().gpaResponse().gpaReportUrl()).isEqualTo(expected.getGpa().getGpaReportUrl()),
                            () -> assertThat(actual.gpaScoreStatusResponse().verifyStatus()).isEqualTo(expected.getVerifyStatus()),

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
            List<GpaScore> expectedGpaScores = List.of(gpaScore1);

            // when
            Page<GpaScoreSearchResponse> response = adminGpaScoreService.searchGpaScores(condition, pageable);

            // then
            assertThat(response.getContent())
                    .hasSize(expectedGpaScores.size())
                    .zipSatisfy(expectedGpaScores, (actual, expected) -> assertAll(
                            () -> assertThat(actual.gpaScoreStatusResponse().id()).isEqualTo(expected.getId()),
                            () -> assertThat(actual.gpaScoreStatusResponse().gpaResponse().gpa()).isEqualTo(expected.getGpa().getGpa()),
                            () -> assertThat(actual.gpaScoreStatusResponse().gpaResponse().gpaCriteria()).isEqualTo(expected.getGpa().getGpaCriteria()),
                            () -> assertThat(actual.gpaScoreStatusResponse().gpaResponse().gpaReportUrl()).isEqualTo(expected.getGpa().getGpaReportUrl()),
                            () -> assertThat(actual.gpaScoreStatusResponse().verifyStatus()).isEqualTo(expected.getVerifyStatus()),

                            () -> assertThat(actual.siteUserResponse().id()).isEqualTo(expected.getSiteUser().getId()),
                            () -> assertThat(actual.siteUserResponse().profileImageUrl()).isEqualTo(expected.getSiteUser().getProfileImageUrl()),
                            () -> assertThat(actual.siteUserResponse().nickname()).isEqualTo(expected.getSiteUser().getNickname())
                    ));
        }
    }

    @Nested
    class GPA_점수_검증_및_수정 {

        @Test
        void GPA와_검증상태를_정상적으로_수정한다() {
            // given
            GpaScoreUpdateRequest request = new GpaScoreUpdateRequest(
                    3.8,
                    4.3,
                    VerifyStatus.APPROVED,
                    null
            );

            // when
            GpaScoreResponse response = adminGpaScoreService.updateGpaScore(gpaScore1.getId(), request);

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(gpaScore1.getId()),
                    () -> assertThat(response.gpa()).isEqualTo(request.gpa()),
                    () -> assertThat(response.gpaCriteria()).isEqualTo(request.gpaCriteria()),
                    () -> assertThat(response.verifyStatus()).isEqualTo(request.verifyStatus()),
                    () -> assertThat(response.rejectedReason()).isNull()
            );
        }

        @Test
        void 승인상태로_변경_시_거절사유가_입력되어도_null로_저장된다() {
            // given
            GpaScoreUpdateRequest request = new GpaScoreUpdateRequest(
                    3.8,
                    4.3,
                    VerifyStatus.APPROVED,
                    "이 거절사유는 무시되어야 함"
            );

            // when
            GpaScoreResponse response = adminGpaScoreService.updateGpaScore(gpaScore1.getId(), request);

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(gpaScore1.getId()),
                    () -> assertThat(response.gpa()).isEqualTo(request.gpa()),
                    () -> assertThat(response.gpaCriteria()).isEqualTo(request.gpaCriteria()),
                    () -> assertThat(response.verifyStatus()).isEqualTo(VerifyStatus.APPROVED),
                    () -> assertThat(response.rejectedReason()).isNull()
            );
        }

        @Test
        void 존재하지_않는_GPA_수정_시_예외_응답을_반환한다() {
            // given
            long invalidGpaScoreId = 9999L;
            GpaScoreUpdateRequest request = new GpaScoreUpdateRequest(
                    3.8,
                    4.3,
                    VerifyStatus.APPROVED,
                    null
            );

            // when & then
            assertThatCode(() -> adminGpaScoreService.updateGpaScore(invalidGpaScoreId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(GPA_SCORE_NOT_FOUND.getMessage());
        }
    }
}
