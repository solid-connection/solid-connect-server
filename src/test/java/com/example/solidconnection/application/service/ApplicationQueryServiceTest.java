package com.example.solidconnection.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.dto.ApplicantResponse;
import com.example.solidconnection.application.dto.ApplicantsResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.fixture.ApplicationFixture;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.location.region.fixture.RegionFixture;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.fixture.GpaScoreFixture;
import com.example.solidconnection.score.fixture.LanguageTestScoreFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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
    private UnivApplyInfoFixture univApplyInfoFixture;

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

    private UnivApplyInfo 괌대학_A_지원_정보;
    private UnivApplyInfo 괌대학_B_지원_정보;
    private UnivApplyInfo 서던덴마크대학교_지원_정보;

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

        괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보();
        괌대학_B_지원_정보 = univApplyInfoFixture.괌대학_B_지원_정보();
        서던덴마크대학교_지원_정보 = univApplyInfoFixture.서던덴마크대학교_지원_정보();
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
                    괌대학_A_지원_정보.getId(),
                    null,
                    null
            );
            Application application2 = applicationFixture.지원서(
                    user2,
                    "nickname2",
                    term,
                    gpaScore2.getGpa(),
                    languageTestScore2.getLanguageTest(),
                    괌대학_B_지원_정보.getId(),
                    null,
                    null
            );
            Application application3 = applicationFixture.지원서(
                    user3,
                    "nickname3",
                    term,
                    gpaScore3.getGpa(),
                    languageTestScore3.getLanguageTest(),
                    서던덴마크대학교_지원_정보.getId(),
                    null,
                    null
            );

            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    user1.getId(),
                    "",
                    ""
            );

            // then
            assertThat(response.firstChoice()).containsAll(List.of(
                    ApplicantsResponse.of(괌대학_A_지원_정보,
                                          List.of(application1), user1),
                    ApplicantsResponse.of(괌대학_B_지원_정보,
                                          List.of(application2), user1),
                    ApplicantsResponse.of(서던덴마크대학교_지원_정보,
                                          List.of(application3), user1)
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
                    괌대학_A_지원_정보.getId(),
                    null,
                    null
            );
            Application application2 = applicationFixture.지원서(
                    user2,
                    "nickname2",
                    term,
                    gpaScore2.getGpa(),
                    languageTestScore2.getLanguageTest(),
                    괌대학_B_지원_정보.getId(),
                    null,
                    null
            );
            applicationFixture.지원서(
                    user3,
                    "nickname3",
                    term,
                    gpaScore3.getGpa(),
                    languageTestScore3.getLanguageTest(),
                    서던덴마크대학교_지원_정보.getId(),
                    null,
                    null
            );

            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    user1.getId(),
                    regionFixture.영미권().getCode(),
                    ""
            );

            // then
            assertThat(response.firstChoice()).containsExactlyInAnyOrder(
                    ApplicantsResponse.of(괌대학_A_지원_정보,
                                          List.of(application1), user1),
                    ApplicantsResponse.of(괌대학_B_지원_정보,
                                          List.of(application2), user1)
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
                    괌대학_A_지원_정보.getId(),
                    null,
                    null
            );
            Application application2 = applicationFixture.지원서(
                    user2,
                    "nickname2",
                    term,
                    gpaScore2.getGpa(),
                    languageTestScore2.getLanguageTest(),
                    괌대학_B_지원_정보.getId(),
                    null,
                    null
            );
            applicationFixture.지원서(
                    user3,
                    "nickname3",
                    term,
                    gpaScore3.getGpa(),
                    languageTestScore3.getLanguageTest(),
                    서던덴마크대학교_지원_정보.getId(),
                    null,
                    null
            );

            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    user1.getId(),
                    null,
                    "괌"
            );

            // then
            assertThat(response.firstChoice()).containsExactlyInAnyOrder(
                    ApplicantsResponse.of(괌대학_A_지원_정보,
                                          List.of(application1), user1),
                    ApplicantsResponse.of(괌대학_B_지원_정보,
                                          List.of(application2), user1)
            );
        }

        @Test
        void 현재_학기_지원서만_조회되고_이전_학기_지원서는_제외된다() {
            // given
            applicationFixture.지원서(
                    user1,
                    "nickname1_past",
                    "1988-1",
                    gpaScore1.getGpa(),
                    languageTestScore1.getLanguageTest(),
                    괌대학_A_지원_정보.getId(),
                    null,
                    null
            );

            Application currentApplication = applicationFixture.지원서(
                    user1,
                    "nickname1",
                    term,
                    gpaScore1.getGpa(),
                    languageTestScore1.getLanguageTest(),
                    괌대학_A_지원_정보.getId(),
                    null,
                    null
            );

            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    user1.getId(),
                    "",
                    ""
            );

            // then
            assertThat(response.firstChoice()).containsExactlyInAnyOrder(
                    ApplicantsResponse.of(괌대학_A_지원_정보, List.of(currentApplication), user1),
                    ApplicantsResponse.of(괌대학_B_지원_정보, List.of(), user1),
                    ApplicantsResponse.of(서던덴마크대학교_지원_정보, List.of(), user1)
            );
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
                    괌대학_A_지원_정보.getId(),
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
                    괌대학_B_지원_정보.getId(),
                    null,
                    null
            );

            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    user1.getId(),
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
                    괌대학_A_지원_정보.getId(),
                    null,
                    null
            );
            Application application2 = applicationFixture.지원서(
                    user2,
                    "nickname2",
                    term,
                    gpaScore2.getGpa(),
                    languageTestScore2.getLanguageTest(),
                    괌대학_A_지원_정보.getId(),
                    null,
                    null
            );
            applicationFixture.지원서(
                    user3,
                    "nickname3",
                    term,
                    gpaScore3.getGpa(),
                    languageTestScore3.getLanguageTest(),
                    서던덴마크대학교_지원_정보.getId(),
                    null,
                    null
            );
            // when
            ApplicationsResponse response = applicationQueryService.getApplicantsByUserApplications(user1.getId());

            // then
            assertThat(response.firstChoice()).containsExactlyInAnyOrder(
                    ApplicantsResponse.of(괌대학_A_지원_정보,
                                          List.of(application1, application2), user1)
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
                    괌대학_A_지원_정보.getId(),
                    null,
                    null
            );
            Application application2 = applicationFixture.지원서(
                    user2,
                    "nickname2",
                    term,
                    gpaScore2.getGpa(),
                    languageTestScore2.getLanguageTest(),
                    괌대학_A_지원_정보.getId(),
                    괌대학_B_지원_정보.getId(),
                    서던덴마크대학교_지원_정보.getId()
            );
            Application application3 = applicationFixture.지원서(
                    user3,
                    "nickname3",
                    term,
                    gpaScore3.getGpa(),
                    languageTestScore3.getLanguageTest(),
                    서던덴마크대학교_지원_정보.getId(),
                    null,
                    null
            );

            // when
            ApplicationsResponse response = applicationQueryService.getApplicantsByUserApplications(user1.getId());

            // then
            assertThat(response.firstChoice())
                    .hasSize(1)
                    .allSatisfy(uar -> {
                        assertThat(uar.koreanName()).isEqualTo(괌대학_A_지원_정보.getKoreanName());
                        assertThat(uar.applicants())
                                .extracting(ApplicantResponse::nicknameForApply)
                                .containsExactlyInAnyOrder("nickname1", "nickname2");
                    });
        }
    }
}
