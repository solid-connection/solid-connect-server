package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.dto.ApplyRequest;
import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.type.VerifyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.solidconnection.application.service.ApplicationSubmissionService.APPLICATION_UPDATE_COUNT_LIMIT;
import static com.example.solidconnection.custom.exception.ErrorCode.APPLY_UPDATE_LIMIT_EXCEED;
import static com.example.solidconnection.custom.exception.ErrorCode.CANT_APPLY_FOR_SAME_UNIVERSITY;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_GPA_SCORE_STATUS;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_LANGUAGE_TEST_SCORE_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("지원서 제출 서비스 테스트")
class ApplicationSubmissionServiceTest extends BaseIntegrationTest {

    @Autowired
    private ApplicationSubmissionService applicationSubmissionService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private GpaScoreRepository gpaScoreRepository;

    @Autowired
    private LanguageTestScoreRepository languageTestScoreRepository;

    @Test
    void 정상적으로_지원서를_제출한다() {
        // given
        GpaScore gpaScore = createApprovedGpaScore(테스트유저_1);
        LanguageTestScore languageTestScore = createApprovedLanguageTestScore(테스트유저_1);
        UniversityChoiceRequest universityChoiceRequest = new UniversityChoiceRequest(
                괌대학_A_지원_정보.getId(),
                네바다주립대학_라스베이거스_지원_정보.getId(),
                메모리얼대학_세인트존스_A_지원_정보.getId()
        );
        ApplyRequest request = new ApplyRequest(gpaScore.getId(), languageTestScore.getId(), universityChoiceRequest);

        // when
        boolean result = applicationSubmissionService.apply(테스트유저_1, request);

        // then
        Application savedApplication = applicationRepository.findBySiteUserAndTerm(테스트유저_1, term).orElseThrow();
        assertAll(
                () -> assertThat(result).isTrue(),
                () -> assertThat(savedApplication.getGpa()).isEqualTo(gpaScore.getGpa()),
                () -> assertThat(savedApplication.getLanguageTest()).isEqualTo(languageTestScore.getLanguageTest()),
                () -> assertThat(savedApplication.getVerifyStatus()).isEqualTo(VerifyStatus.APPROVED),
                () -> assertThat(savedApplication.getNicknameForApply()).isNotNull(),
                () -> assertThat(savedApplication.getUpdateCount()).isZero(),
                () -> assertThat(savedApplication.getTerm()).isEqualTo(term),
                () -> assertThat(savedApplication.isDelete()).isFalse(),
                () -> assertThat(savedApplication.getFirstChoiceUniversity().getId()).isEqualTo(괌대학_A_지원_정보.getId()),
                () -> assertThat(savedApplication.getSecondChoiceUniversity().getId()).isEqualTo(네바다주립대학_라스베이거스_지원_정보.getId()),
                () -> assertThat(savedApplication.getThirdChoiceUniversity().getId()).isEqualTo(메모리얼대학_세인트존스_A_지원_정보.getId()),
                () -> assertThat(savedApplication.getSiteUser().getId()).isEqualTo(테스트유저_1.getId())
        );
    }

    @Test
    void 미승인된_GPA_성적으로_지원하면_예외_응답을_반환한다() {
        // given
        GpaScore gpaScore = createUnapprovedGpaScore(테스트유저_1);
        LanguageTestScore languageTestScore = createApprovedLanguageTestScore(테스트유저_1);
        UniversityChoiceRequest universityChoiceRequest = new UniversityChoiceRequest(
                괌대학_A_지원_정보.getId(),
                null,
                null
        );
        ApplyRequest request = new ApplyRequest(gpaScore.getId(), languageTestScore.getId(), universityChoiceRequest);

        // when & then
        assertThatCode(() ->
                applicationSubmissionService.apply(테스트유저_1, request)
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_GPA_SCORE_STATUS.getMessage());
    }

    @Test
    void 미승인된_어학성적으로_지원하면_예외_응답을_반환한다() {
        // given
        GpaScore gpaScore = createApprovedGpaScore(테스트유저_1);
        LanguageTestScore languageTestScore = createUnapprovedLanguageTestScore(테스트유저_1);
        UniversityChoiceRequest universityChoiceRequest = new UniversityChoiceRequest(
                괌대학_A_지원_정보.getId(),
                null,
                null
        );
        ApplyRequest request = new ApplyRequest(gpaScore.getId(), languageTestScore.getId(), universityChoiceRequest);

        // when & then
        assertThatCode(() ->
                applicationSubmissionService.apply(테스트유저_1, request)
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_LANGUAGE_TEST_SCORE_STATUS.getMessage());
    }

    @Test
    void 지원서_수정_횟수를_초과하면_예외_응답을_반환한다() {
        // given
        GpaScore gpaScore = createApprovedGpaScore(테스트유저_1);
        LanguageTestScore languageTestScore = createApprovedLanguageTestScore(테스트유저_1);
        UniversityChoiceRequest universityChoiceRequest = new UniversityChoiceRequest(
                괌대학_A_지원_정보.getId(),
                null,
                null
        );
        ApplyRequest request = new ApplyRequest(gpaScore.getId(), languageTestScore.getId(), universityChoiceRequest);

        for (int i = 0; i < APPLICATION_UPDATE_COUNT_LIMIT + 1; i++) {
            applicationSubmissionService.apply(테스트유저_1, request);
        }

        // when & then
        assertThatCode(() ->
                applicationSubmissionService.apply(테스트유저_1, request)
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(APPLY_UPDATE_LIMIT_EXCEED.getMessage());
    }

    private GpaScore createUnapprovedGpaScore(SiteUser siteUser) {
        GpaScore gpaScore = new GpaScore(
                new Gpa(4.0,  4.5, "/gpa-report.pdf"),
                siteUser
        );
        return gpaScoreRepository.save(gpaScore);
    }

    private GpaScore createApprovedGpaScore(SiteUser siteUser) {
        GpaScore gpaScore = new GpaScore(
                new Gpa(4.0, 4.5, "/gpa-report.pdf"),
                siteUser
        );
        gpaScore.setVerifyStatus(VerifyStatus.APPROVED);
        return gpaScoreRepository.save(gpaScore);
    }

    private LanguageTestScore createUnapprovedLanguageTestScore(SiteUser siteUser) {
        LanguageTestScore languageTestScore = new LanguageTestScore(
                new LanguageTest(LanguageTestType.TOEIC, "100", "/gpa-report.pdf"),
                siteUser
        );
        return languageTestScoreRepository.save(languageTestScore);
    }

    private LanguageTestScore createApprovedLanguageTestScore(SiteUser siteUser) {
        LanguageTestScore languageTestScore = new LanguageTestScore(
                new LanguageTest(LanguageTestType.TOEIC, "100", "/gpa-report.pdf"),
                siteUser
        );
        languageTestScore.setVerifyStatus(VerifyStatus.APPROVED);
        return languageTestScoreRepository.save(languageTestScore);
    }
}
