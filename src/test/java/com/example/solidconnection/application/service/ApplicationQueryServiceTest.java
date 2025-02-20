package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.dto.ApplicantResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.UniversityApplicantsResponse;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.type.VerifyStatus;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지원서 조회 서비스 테스트")
class ApplicationQueryServiceTest extends BaseIntegrationTest {

    @Autowired
    private ApplicationQueryService applicationQueryService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private GpaScoreRepository gpaScoreRepository;

    @Autowired
    private LanguageTestScoreRepository languageTestScoreRepository;

    @Nested
    class 지원자_목록_조회_테스트 {

        @Test
        void 이번_학기_전체_지원자를_조회한다() {
            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    테스트유저_2,
                    "",
                    ""
            );

            // then
            assertThat(response.firstChoice()).containsAll(List.of(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, false))),
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true))),
                    UniversityApplicantsResponse.of(메이지대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_4_메이지대학_그라츠대학_서던덴마크대학_지원서, false))),
                    UniversityApplicantsResponse.of(네바다주립대학_라스베이거스_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_5_네바다주립대학_그라츠공과대학_메이지대학_지원서, false))),
                    UniversityApplicantsResponse.of(코펜하겐IT대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_7_코펜하겐IT대학_X_X_지원서, false)))
            ));

            assertThat(response.secondChoice()).containsAll(List.of(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true))),
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, false))),
                    UniversityApplicantsResponse.of(그라츠대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_4_메이지대학_그라츠대학_서던덴마크대학_지원서, false))),
                    UniversityApplicantsResponse.of(그라츠공과대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_5_네바다주립대학_그라츠공과대학_메이지대학_지원서, false)))
            ));

            assertThat(response.thirdChoice()).containsAll(List.of(
                    UniversityApplicantsResponse.of(린츠_카톨릭대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true))),
                    UniversityApplicantsResponse.of(그라츠공과대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, false))),
                    UniversityApplicantsResponse.of(서던덴마크대학교_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_4_메이지대학_그라츠대학_서던덴마크대학_지원서, false))),
                    UniversityApplicantsResponse.of(메이지대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_5_네바다주립대학_그라츠공과대학_메이지대학_지원서, false)))
            ));
        }

        @Test
        void 이번_학기_특정_지역_지원자를_조회한다() {
            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    테스트유저_2,
                    영미권.getCode(),
                    ""
            );

            // then
            assertThat(response.firstChoice()).containsAll(List.of(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, false))),
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true))),
                    UniversityApplicantsResponse.of(네바다주립대학_라스베이거스_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_5_네바다주립대학_그라츠공과대학_메이지대학_지원서, false)))
            ));

            assertThat(response.secondChoice()).containsAll(List.of(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true))),
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, false)))
            ));
        }

        @Test
        void 이번_학기_지원자를_대학_국문_이름으로_필터링해서_조회한다() {
            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    테스트유저_2,
                    null,
                    "일본"
            );

            // then
            assertThat(response.firstChoice()).containsAll(List.of(
                    UniversityApplicantsResponse.of(메이지대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_4_메이지대학_그라츠대학_서던덴마크대학_지원서, false)))
            ));

            assertThat(response.secondChoice()).containsAll(List.of(
                    UniversityApplicantsResponse.of(메이지대학_지원_정보, List.of())
            ));

            assertThat(response.thirdChoice()).containsExactlyInAnyOrder(
                    UniversityApplicantsResponse.of(메이지대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_5_네바다주립대학_그라츠공과대학_메이지대학_지원서, false)))
            );
        }

        @Test
        void 이전_학기_지원자는_조회되지_않는다() {
            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    테스트유저_1,
                    "",
                    ""
            );

            // then
            assertThat(response.firstChoice()).doesNotContainAnyElementsOf(List.of(
                    UniversityApplicantsResponse.of(네바다주립대학_라스베이거스_지원_정보,
                            List.of(ApplicantResponse.of(이전학기_지원서, false)))
            ));
            assertThat(response.secondChoice()).doesNotContainAnyElementsOf(List.of(
                    UniversityApplicantsResponse.of(그라츠공과대학_지원_정보,
                            List.of(ApplicantResponse.of(이전학기_지원서, false)))
            ));
            assertThat(response.thirdChoice()).doesNotContainAnyElementsOf(List.of(
                    UniversityApplicantsResponse.of(메이지대학_지원_정보,
                            List.of(ApplicantResponse.of(이전학기_지원서, false)))
            ));
        }

        @Test
        void 동일_유저의_여러_지원서_중_최신_지원서만_조회된다() {
            // given
            Application firstApplication = createApplication(테스트유저_1, 괌대학_A_지원_정보);
            firstApplication.setIsDeleteTrue();
            applicationRepository.save(firstApplication);
            Application secondApplication = createApplication(테스트유저_1, 네바다주립대학_라스베이거스_지원_정보);


            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    테스트유저_1, "", "");

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
            // when
            ApplicationsResponse response = applicationQueryService.getApplicantsByUserApplications(
                    테스트유저_2
            );

            // then
            assertThat(response.firstChoice()).containsAll(List.of(
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true))),
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, false)))
            ));

            assertThat(response.secondChoice()).containsAll(List.of(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true))),
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, false)))
            ));

            assertThat(response.thirdChoice()).containsAll(List.of(
                    UniversityApplicantsResponse.of(린츠_카톨릭대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true)))
            ));
        }

        @Test
        void 이번_학기_지원한_대학_중_미선택이_있을_때_경쟁자_목록을_조회한다() {
            // when
            ApplicationsResponse response = applicationQueryService.getApplicantsByUserApplications(
                    테스트유저_7
            );

            // then
            assertThat(response.firstChoice()).containsAll(List.of(
                    UniversityApplicantsResponse.of(코펜하겐IT대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_7_코펜하겐IT대학_X_X_지원서, true)))
            ));

            assertThat(response.secondChoice()).containsExactlyInAnyOrder(
                    UniversityApplicantsResponse.of(코펜하겐IT대학_지원_정보, List.of())
            );

            assertThat(response.thirdChoice()).containsExactlyInAnyOrder(
                    UniversityApplicantsResponse.of(코펜하겐IT대학_지원_정보, List.of())
            );
        }

        @Test
        void 이번_학기_지원한_대학이_모두_미선택일_때_경쟁자_목록을_조회한다() {
            //when
            ApplicationsResponse response = applicationQueryService.getApplicantsByUserApplications(
                    테스트유저_6
            );

            // then
            assertThat(response.firstChoice()).isEmpty();
            assertThat(response.secondChoice()).isEmpty();
            assertThat(response.thirdChoice()).isEmpty();
        }
    }

    private GpaScore createApprovedGpaScore(SiteUser siteUser) {
        GpaScore gpaScore = new GpaScore(
                new Gpa(4.0, 4.5, "/gpa-report.pdf"),
                siteUser
        );
        gpaScore.setVerifyStatus(VerifyStatus.APPROVED);
        return gpaScoreRepository.save(gpaScore);
    }

    private LanguageTestScore createApprovedLanguageTestScore(SiteUser siteUser) {
        LanguageTestScore languageTestScore = new LanguageTestScore(
                new LanguageTest(LanguageTestType.TOEIC, "100", "/gpa-report.pdf"),
                siteUser
        );
        languageTestScore.setVerifyStatus(VerifyStatus.APPROVED);
        return languageTestScoreRepository.save(languageTestScore);
    }

    private Application createApplication(
            SiteUser siteUser,
            UniversityInfoForApply universityInfoForApply) {
        Application application = new Application(
                siteUser,
                createApprovedGpaScore(siteUser).getGpa(),
                createApprovedLanguageTestScore(siteUser).getLanguageTest(),
                term,
                universityInfoForApply,
                null,
                null,
                null
        );
        application.setVerifyStatus(VerifyStatus.APPROVED);
        return applicationRepository.save(application);
    }
}
