package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.VerifyStatus;
import com.example.solidconnection.application.dto.ApplicantResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.UniversityApplicantsResponse;
import com.example.solidconnection.application.fixture.ApplicationFixture;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.location.region.fixture.RegionFixture;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.fixture.GpaScoreFixture;
import com.example.solidconnection.score.fixture.LanguageTestScoreFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.fixture.LanguageRequirementFixture;
import com.example.solidconnection.university.fixture.UniversityInfoForApplyFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestContainerSpringBootTest
@DisplayName("지원서 조회 서비스 테스트")
class ApplicationQueryServiceTest {

    @Autowired
    private ApplicationQueryService applicationQueryService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private RegionFixture regionFixture;

    @Autowired
    private UniversityInfoForApplyFixture universityInfoForApplyFixture;

    @Autowired
    private LanguageRequirementFixture languageRequirementFixture;

    @Autowired
    private GpaScoreFixture gpaScoreFixture;

    @Autowired
    private LanguageTestScoreFixture languageTestScoreFixture;

    @Autowired
    private ApplicationFixture applicationFixture;

    @Value("${university.term}")
    private String term;

    private SiteUser user1;
    private SiteUser user2;
    private SiteUser user3;

    private GpaScore gpaScore1;
    private GpaScore gpaScore2;
    private GpaScore gpaScore3;

    private LanguageTestScore languageTestScore1;
    private LanguageTestScore languageTestScore2;
    private LanguageTestScore languageTestScore3;

    private UniversityInfoForApply 괌대학_A_지원_정보;
    private UniversityInfoForApply 괌대학_B_지원_정보;
    private UniversityInfoForApply 서던덴마크대학교_지원_정보;

    @BeforeEach
    void setUp() {
        user1 = siteUserFixture.사용자(1, "test1");
        gpaScore1 = gpaScoreFixture.GPA_점수(VerifyStatus.APPROVED, user1);
        languageTestScore1 = languageTestScoreFixture.어학_점수(VerifyStatus.APPROVED, user1);

        user2 = siteUserFixture.사용자(2, "test2");
        gpaScore2 = gpaScoreFixture.GPA_점수(VerifyStatus.APPROVED, user2);
        languageTestScore2 = languageTestScoreFixture.어학_점수(VerifyStatus.APPROVED, user2);

        user3 = siteUserFixture.사용자(3, "test3");
        gpaScore3 = gpaScoreFixture.GPA_점수(VerifyStatus.APPROVED, user3);
        languageTestScore3 = languageTestScoreFixture.어학_점수(VerifyStatus.APPROVED, user3);

        괌대학_A_지원_정보 = universityInfoForApplyFixture.괌대학_A_지원_정보();
        languageRequirementFixture.토플_80(괌대학_A_지원_정보);
        languageRequirementFixture.토익_800(괌대학_A_지원_정보);

        괌대학_B_지원_정보 = universityInfoForApplyFixture.괌대학_B_지원_정보();
        languageRequirementFixture.토플_70(괌대학_B_지원_정보);
        languageRequirementFixture.토익_900(괌대학_B_지원_정보);

        서던덴마크대학교_지원_정보 = universityInfoForApplyFixture.서던덴마크대학교_지원_정보();
        languageRequirementFixture.토플_70(서던덴마크대학교_지원_정보);
    }

    @Nested
    class 지원자_목록_조회_테스트 {

        @Test
        void 이번_학기_전체_지원자를_조회한다() {
            // given
            Application application1 = applicationFixture.지원서(
                    user1,
                    "nickname1",
                    term,
                    gpaScore1.getGpa(),
                    languageTestScore1.getLanguageTest(),
                    괌대학_A_지원_정보,
                    null,
                    null
            );
            Application application2 = applicationFixture.지원서(
                    user2,
                    "nickname2",
                    term,
                    gpaScore2.getGpa(),
                    languageTestScore2.getLanguageTest(),
                    괌대학_B_지원_정보,
                    null,
                    null
            );
            Application application3 = applicationFixture.지원서(
                    user3,
                    "nickname3",
                    term,
                    gpaScore3.getGpa(),
                    languageTestScore3.getLanguageTest(),
                    서던덴마크대학교_지원_정보,
                    null,
                    null
            );

            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    user1,
                    "",
                    ""
            );

            // then
            assertThat(response.firstChoice()).containsAll(List.of(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(application1, true))),
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(application2, false))),
                    UniversityApplicantsResponse.of(서던덴마크대학교_지원_정보,
                            List.of(ApplicantResponse.of(application3, false)))
            ));
        }

        @Test
        void 이번_학기_특정_지역_지원자를_조회한다() {
            //given
            Application application1 = applicationFixture.지원서(
                    user1,
                    "nickname1",
                    term,
                    gpaScore1.getGpa(),
                    languageTestScore1.getLanguageTest(),
                    괌대학_A_지원_정보,
                    null,
                    null
            );
            Application application2 = applicationFixture.지원서(
                    user2,
                    "nickname2",
                    term,
                    gpaScore2.getGpa(),
                    languageTestScore2.getLanguageTest(),
                    괌대학_B_지원_정보,
                    null,
                    null
            );
            applicationFixture.지원서(
                    user3,
                    "nickname3",
                    term,
                    gpaScore3.getGpa(),
                    languageTestScore3.getLanguageTest(),
                    서던덴마크대학교_지원_정보,
                    null,
                    null
            );

            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    user1,
                    regionFixture.영미권().getCode(),
                    ""
            );

            // then
            assertThat(response.firstChoice()).containsExactlyInAnyOrder(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(application1, true))),
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(application2, false)))
            );
        }

        @Test
        void 이번_학기_지원자를_대학_국문_이름으로_필터링해서_조회한다() {
            //given
            Application application1 = applicationFixture.지원서(
                    user1,
                    "nickname1",
                    term,
                    gpaScore1.getGpa(),
                    languageTestScore1.getLanguageTest(),
                    괌대학_A_지원_정보,
                    null,
                    null
            );
            Application application2 = applicationFixture.지원서(
                    user2,
                    "nickname2",
                    term,
                    gpaScore2.getGpa(),
                    languageTestScore2.getLanguageTest(),
                    괌대학_B_지원_정보,
                    null,
                    null
            );
            applicationFixture.지원서(
                    user3,
                    "nickname3",
                    term,
                    gpaScore3.getGpa(),
                    languageTestScore3.getLanguageTest(),
                    서던덴마크대학교_지원_정보,
                    null,
                    null
            );

            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    user1,
                    null,
                    "괌"
            );

            // then
            assertThat(response.firstChoice()).containsExactlyInAnyOrder(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(application1, true))),
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(application2, false)))
            );
        }

        @Test
        void 이전_학기_지원자는_조회되지_않는다() {
            // given
            Application application = applicationFixture.지원서(
                    user1,
                    "nickname1",
                    "1988-1",
                    gpaScore1.getGpa(),
                    languageTestScore1.getLanguageTest(),
                    괌대학_A_지원_정보,
                    null,
                    null
            );

            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    user1,
                    "",
                    ""
            );

            // then
            assertThat(response.firstChoice()).doesNotContainAnyElementsOf(List.of(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(application, true)))
            ));
        }

        @Test
        void 동일_유저의_여러_지원서_중_최신_지원서만_조회된다() {
            // given
            Application firstApplication = applicationFixture.지원서(
                    user1,
                    "nickname1",
                    term,
                    gpaScore1.getGpa(),
                    languageTestScore1.getLanguageTest(),
                    괌대학_A_지원_정보,
                    null,
                    null
            );
            firstApplication.setIsDeleteTrue();
            applicationRepository.save(firstApplication);
            Application secondApplication = applicationFixture.지원서(
                    user1,
                    "nickname2",
                    term,
                    gpaScore1.getGpa(),
                    languageTestScore1.getLanguageTest(),
                    괌대학_B_지원_정보,
                    null,
                    null
            );

            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    user1,
                    "",
                    ""
            );

            // then
            assertThat(response.firstChoice().stream()
                    .flatMap(univ -> univ.applicants().stream())
                    .filter(ApplicantResponse::isMine))
                    .containsExactly(ApplicantResponse.of(secondApplication, true));
        }
    }

    @Nested
    class 경쟁자_목록_조회_테스트 {

        @Test
        void 이번_학기_지원한_대학의_경쟁자_목록을_조회한다() {
            // given
            Application application1 = applicationFixture.지원서(
                    user1,
                    "nickname1",
                    term,
                    gpaScore1.getGpa(),
                    languageTestScore1.getLanguageTest(),
                    괌대학_A_지원_정보,
                    null,
                    null
            );
            Application application2 = applicationFixture.지원서(
                    user2,
                    "nickname2",
                    term,
                    gpaScore2.getGpa(),
                    languageTestScore2.getLanguageTest(),
                    괌대학_A_지원_정보,
                    null,
                    null
            );
            applicationFixture.지원서(
                    user3,
                    "nickname3",
                    term,
                    gpaScore3.getGpa(),
                    languageTestScore3.getLanguageTest(),
                    괌대학_B_지원_정보,
                    null,
                    null
            );
            // when
            ApplicationsResponse response = applicationQueryService.getApplicantsByUserApplications(user1);

            // then
            assertThat(response.firstChoice()).containsExactlyInAnyOrder(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보, List.of(
                            ApplicantResponse.of(application1, true),
                            ApplicantResponse.of(application2, false)
                    ))
            );
        }

        @Test
        void 이번_학기_지원한_대학_중_미선택이_있을_때_경쟁자_목록을_조회한다() {
            // given
            Application application1 = applicationFixture.지원서(
                    user1,
                    "nickname1",
                    term,
                    gpaScore1.getGpa(),
                    languageTestScore1.getLanguageTest(),
                    괌대학_A_지원_정보,
                    null,
                    null
            );
            applicationFixture.지원서(
                    user2,
                    "nickname2",
                    term,
                    gpaScore2.getGpa(),
                    languageTestScore2.getLanguageTest(),
                    null,
                    괌대학_B_지원_정보,
                    null
            );
            applicationFixture.지원서(
                    user3,
                    "nickname3",
                    term,
                    gpaScore3.getGpa(),
                    languageTestScore3.getLanguageTest(),
                    null,
                    null,
                    서던덴마크대학교_지원_정보
            );

            // when
            ApplicationsResponse response = applicationQueryService.getApplicantsByUserApplications(user1);

            // then
            assertThat(response.firstChoice()).containsExactlyInAnyOrder(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(application1, true)))
            );

            assertThat(response.secondChoice()).containsExactlyInAnyOrder(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보, List.of())
            );

            assertThat(response.thirdChoice()).containsExactlyInAnyOrder(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보, List.of())
            );
        }
    }
}
