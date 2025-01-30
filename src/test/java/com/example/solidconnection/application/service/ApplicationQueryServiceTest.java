package com.example.solidconnection.application.service;

import com.example.solidconnection.application.dto.ApplicantResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.UniversityApplicantsResponse;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
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

    @Nested
    class 지원자_목록_조회_테스트 {

        @Test
        void 이번학기_전체_지원자를_조회한다() {
            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    테스트유저_1.getEmail(),
                    "",
                    ""
            );

            // then
            assertThat(response.firstChoice()).containsAnyElementsOf(List.of(
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

            assertThat(response.firstChoice()).containsAnyElementsOf(List.of(
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

            assertThat(response.secondChoice()).containsAnyElementsOf(List.of(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true))),
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, false))),
                    UniversityApplicantsResponse.of(그라츠대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_4_메이지대학_그라츠대학_서던덴마크대학_지원서, false))),
                    UniversityApplicantsResponse.of(그라츠공과대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_5_네바다주립대학_그라츠공과대학_메이지대학_지원서, false)))
            ));

            assertThat(response.thirdChoice()).containsAnyElementsOf(List.of(
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
        void 이번학기_특정_지역_지원자를_조회한다() {
            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    테스트유저_1.getEmail(),
                    영미권.getCode(),
                    ""
            );

            // then
            assertThat(response.firstChoice()).containsAnyElementsOf(List.of(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, false))),
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true))),
                    UniversityApplicantsResponse.of(네바다주립대학_라스베이거스_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_5_네바다주립대학_그라츠공과대학_메이지대학_지원서, false)))
            ));

            assertThat(response.secondChoice()).containsAnyElementsOf(List.of(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true))),
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, false)))
            ));
        }

        @Test
        void 이번학기_지원자를_대학_국문_이름으로_필터링해서_조회한다() {
            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    테스트유저_1.getEmail(),
                    null,
                    "일본"
            );

            // then
            assertThat(response.firstChoice()).containsExactlyInAnyOrder(
                    UniversityApplicantsResponse.of(메이지대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_4_메이지대학_그라츠대학_서던덴마크대학_지원서, false)))
            );

            assertThat(response.secondChoice()).containsExactlyInAnyOrder(
                    UniversityApplicantsResponse.of(메이지대학_지원_정보, List.of())
            );

            assertThat(response.thirdChoice()).containsExactlyInAnyOrder(
                    UniversityApplicantsResponse.of(메이지대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_5_네바다주립대학_그라츠공과대학_메이지대학_지원서, false)))
            );
        }

        @Test
        void 이전학기_지원자는_조회되지_않는다() {
            // when
            ApplicationsResponse response = applicationQueryService.getApplicants(
                    테스트유저_1.getEmail(),
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
    }

    @Nested
    class 경쟁자_목록_조회_테스트 {

        @Test
        void 이번학기_지원한_대학의_경쟁자_목록을_조회한다() {
            // when
            ApplicationsResponse response = applicationQueryService.getApplicantsByUserApplications(
                    테스트유저_2.getEmail()
            );

            // then
            assertThat(response.firstChoice()).containsAnyElementsOf(List.of(
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true))),
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, false)))
            ));

            assertThat(response.secondChoice()).containsAnyElementsOf(List.of(
                    UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true))),
                    UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, false)))
            ));

            assertThat(response.thirdChoice()).containsAnyElementsOf(List.of(
                    UniversityApplicantsResponse.of(린츠_카톨릭대학_지원_정보,
                            List.of(ApplicantResponse.of(테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, true)))
            ));
        }

        @Test
        void 이번학기_지원한_대학중_미선택이_있을_때_경쟁자_목록을_조회한다() {
            // when
            ApplicationsResponse response = applicationQueryService.getApplicantsByUserApplications(
                    테스트유저_7.getEmail()
            );

            // then
            assertThat(response.firstChoice()).containsAnyElementsOf(List.of(
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
        void 이번학기_지원한_대학이_모두_미선택일_때_경쟁자_목록을_조회한다() {
            //when
            ApplicationsResponse response = applicationQueryService.getApplicantsByUserApplications(
                    테스트유저_6.getEmail()
            );

            // then
            assertThat(response.firstChoice()).isEmpty();
            assertThat(response.secondChoice()).isEmpty();
            assertThat(response.thirdChoice()).isEmpty();
        }
    }
}
