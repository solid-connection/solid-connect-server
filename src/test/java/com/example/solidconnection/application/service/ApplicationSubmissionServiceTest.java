package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.VerifyStatus;
import com.example.solidconnection.application.dto.ApplicationSubmissionResponse;
import com.example.solidconnection.application.dto.ApplyRequest;
import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.fixture.GpaScoreFixture;
import com.example.solidconnection.score.fixture.LanguageTestScoreFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.fixture.UniversityInfoForApplyFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static com.example.solidconnection.application.service.ApplicationSubmissionService.APPLICATION_UPDATE_COUNT_LIMIT;
import static com.example.solidconnection.common.exception.ErrorCode.APPLY_UPDATE_LIMIT_EXCEED;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_GPA_SCORE_STATUS;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_LANGUAGE_TEST_SCORE_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
@DisplayName("지원서 제출 서비스 테스트")
class ApplicationSubmissionServiceTest {

    @Autowired
    private ApplicationSubmissionService applicationSubmissionService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private UniversityInfoForApplyFixture universityInfoForApplyFixture;

    @Autowired
    private GpaScoreFixture gpaScoreFixture;

    @Autowired
    private LanguageTestScoreFixture languageTestScoreFixture;

    @Value("${university.term}")
    private String term;

    private SiteUser user;
    private UniversityInfoForApply 괌대학_A_지원_정보;
    private UniversityInfoForApply 괌대학_B_지원_정보;
    private UniversityInfoForApply 서던덴마크대학교_지원_정보;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
        괌대학_A_지원_정보 = universityInfoForApplyFixture.괌대학_A_지원_정보();
        괌대학_B_지원_정보 = universityInfoForApplyFixture.괌대학_B_지원_정보();
        서던덴마크대학교_지원_정보 = universityInfoForApplyFixture.서던덴마크대학교_지원_정보();
    }

    @Test
    void 정상적으로_지원서를_제출한다() {
        // given
        GpaScore gpaScore = gpaScoreFixture.GPA_점수(VerifyStatus.APPROVED, user);
        LanguageTestScore languageTestScore = languageTestScoreFixture.어학_점수(VerifyStatus.APPROVED, user);
        UniversityChoiceRequest universityChoiceRequest = new UniversityChoiceRequest(
                괌대학_A_지원_정보.getId(),
                괌대학_B_지원_정보.getId(),
                서던덴마크대학교_지원_정보.getId()
        );
        ApplyRequest request = new ApplyRequest(gpaScore.getId(), languageTestScore.getId(), universityChoiceRequest);

        // when
        ApplicationSubmissionResponse response = applicationSubmissionService.apply(user, request);

        // then
        Application savedApplication = applicationRepository.findBySiteUserAndTerm(user, term).orElseThrow();
        assertAll(
                () -> assertThat(response.applyCount())
                        .isEqualTo(savedApplication.getUpdateCount()),
                () -> assertThat(savedApplication.getVerifyStatus())
                        .isEqualTo(VerifyStatus.APPROVED),
                () -> assertThat(savedApplication.isDelete())
                        .isFalse(),
                () -> assertThat(savedApplication.getFirstChoiceUniversityApplyInfoId())
                        .isEqualTo(괌대학_A_지원_정보.getId()),
                () -> assertThat(savedApplication.getSecondChoiceUniversityApplyInfoId())
                        .isEqualTo(괌대학_B_지원_정보.getId()),
                () -> assertThat(savedApplication.getThirdChoiceUniversityApplyInfoId())
                        .isEqualTo(서던덴마크대학교_지원_정보.getId())
        );
    }

    @Test
    void 미승인된_GPA_성적으로_지원하면_예외_응답을_반환한다() {
        // given
        GpaScore gpaScore = gpaScoreFixture.GPA_점수(VerifyStatus.PENDING, user);
        LanguageTestScore languageTestScore = languageTestScoreFixture.어학_점수(VerifyStatus.APPROVED, user);
        UniversityChoiceRequest universityChoiceRequest = new UniversityChoiceRequest(
                괌대학_A_지원_정보.getId(),
                null,
                null
        );
        ApplyRequest request = new ApplyRequest(gpaScore.getId(), languageTestScore.getId(), universityChoiceRequest);

        // when & then
        assertThatCode(() ->
                applicationSubmissionService.apply(user, request)
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_GPA_SCORE_STATUS.getMessage());
    }

    @Test
    void 미승인된_어학성적으로_지원하면_예외_응답을_반환한다() {
        // given
        GpaScore gpaScore = gpaScoreFixture.GPA_점수(VerifyStatus.APPROVED, user);
        LanguageTestScore languageTestScore = languageTestScoreFixture.어학_점수(VerifyStatus.PENDING, user);
        UniversityChoiceRequest universityChoiceRequest = new UniversityChoiceRequest(
                괌대학_A_지원_정보.getId(),
                null,
                null
        );
        ApplyRequest request = new ApplyRequest(gpaScore.getId(), languageTestScore.getId(), universityChoiceRequest);

        // when & then
        assertThatCode(() ->
                applicationSubmissionService.apply(user, request)
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_LANGUAGE_TEST_SCORE_STATUS.getMessage());
    }

    @Test
    void 지원서_수정_횟수를_초과하면_예외_응답을_반환한다() {
        // given
        GpaScore gpaScore = gpaScoreFixture.GPA_점수(VerifyStatus.APPROVED, user);
        LanguageTestScore languageTestScore = languageTestScoreFixture.어학_점수(VerifyStatus.APPROVED, user);
        UniversityChoiceRequest universityChoiceRequest = new UniversityChoiceRequest(
                괌대학_A_지원_정보.getId(),
                null,
                null
        );
        ApplyRequest request = new ApplyRequest(gpaScore.getId(), languageTestScore.getId(), universityChoiceRequest);

        for (int i = 0; i < APPLICATION_UPDATE_COUNT_LIMIT; i++) {
            applicationSubmissionService.apply(user, request);
        }

        // when & then
        assertThatCode(() ->
                applicationSubmissionService.apply(user, request)
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(APPLY_UPDATE_LIMIT_EXCEED.getMessage());
    }
}
