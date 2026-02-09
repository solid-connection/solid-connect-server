package com.example.solidconnection.application.service;

import static com.example.solidconnection.application.service.ApplicationSubmissionService.APPLICATION_UPDATE_COUNT_LIMIT;
import static com.example.solidconnection.common.exception.ErrorCode.APPLY_UPDATE_LIMIT_EXCEED;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_GPA_SCORE_STATUS;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_LANGUAGE_TEST_SCORE_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.dto.ApplicationSubmissionResponse;
import com.example.solidconnection.application.dto.ApplyRequest;
import com.example.solidconnection.application.dto.UnivApplyInfoChoiceRequest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.fixture.GpaScoreFixture;
import com.example.solidconnection.score.fixture.LanguageTestScoreFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.fixture.TermFixture;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    private UnivApplyInfoFixture univApplyInfoFixture;

    @Autowired
    private GpaScoreFixture gpaScoreFixture;

    @Autowired
    private LanguageTestScoreFixture languageTestScoreFixture;

    @Autowired
    private TermFixture termFixture;

    private SiteUser user;
    private UnivApplyInfo 괌대학_A_지원_정보;
    private UnivApplyInfo 버지니아공과대학_지원_정보;
    private UnivApplyInfo 서던덴마크대학교_지원_정보;

    private Term term;

    @BeforeEach
    void setUp() {
        term = termFixture.현재_학기("2025-2");

        user = siteUserFixture.사용자();
        괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보(term.getId());
        버지니아공과대학_지원_정보 = univApplyInfoFixture.버지니아공과대학_지원_정보(term.getId());
        서던덴마크대학교_지원_정보 = univApplyInfoFixture.서던덴마크대학교_지원_정보(term.getId());
    }

    @Test
    void 정상적으로_지원서를_제출한다() {
        // given
        GpaScore gpaScore = gpaScoreFixture.GPA_점수(VerifyStatus.APPROVED, user);
        LanguageTestScore languageTestScore = languageTestScoreFixture.어학_점수(VerifyStatus.APPROVED, user);
        UnivApplyInfoChoiceRequest univApplyInfoChoiceRequest = new UnivApplyInfoChoiceRequest(
                괌대학_A_지원_정보.getId(),
                버지니아공과대학_지원_정보.getId(),
                서던덴마크대학교_지원_정보.getId()
        );
        ApplyRequest request = new ApplyRequest(gpaScore.getId(), languageTestScore.getId(), univApplyInfoChoiceRequest);

        // when
        ApplicationSubmissionResponse response = applicationSubmissionService.apply(user.getId(), request);

        // then
        Application savedApplication = applicationRepository.findBySiteUserIdAndTermId(user.getId(), term.getId()).orElseThrow();
        assertAll(
                () -> assertThat(response.totalApplyCount())
                        .isEqualTo(APPLICATION_UPDATE_COUNT_LIMIT),
                () -> assertThat(response.applyCount())
                        .isEqualTo(savedApplication.getUpdateCount()),
                () -> assertThat(response.appliedUniversities().firstChoiceUnivApplyInfo())
                        .isEqualTo(괌대학_A_지원_정보.getKoreanName()),
                () -> assertThat(response.appliedUniversities().secondChoiceUnivApplyInfo())
                        .isEqualTo(버지니아공과대학_지원_정보.getKoreanName()),
                () -> assertThat(response.appliedUniversities().thirdChoiceUnivApplyInfo())
                        .isEqualTo(서던덴마크대학교_지원_정보.getKoreanName()),
                () -> assertThat(savedApplication.getVerifyStatus())
                        .isEqualTo(VerifyStatus.APPROVED),
                () -> assertThat(savedApplication.isDelete())
                        .isFalse(),
                () -> assertThat(savedApplication.getFirstChoiceUnivApplyInfoId())
                        .isEqualTo(괌대학_A_지원_정보.getId()),
                () -> assertThat(savedApplication.getSecondChoiceUnivApplyInfoId())
                        .isEqualTo(버지니아공과대학_지원_정보.getId()),
                () -> assertThat(savedApplication.getThirdChoiceUnivApplyInfoId())
                        .isEqualTo(서던덴마크대학교_지원_정보.getId())
        );
    }

    @Test
    void 미승인된_GPA_성적으로_지원하면_예외가_발생한다() {
        // given
        GpaScore gpaScore = gpaScoreFixture.GPA_점수(VerifyStatus.PENDING, user);
        LanguageTestScore languageTestScore = languageTestScoreFixture.어학_점수(VerifyStatus.APPROVED, user);
        UnivApplyInfoChoiceRequest univApplyInfoChoiceRequest = new UnivApplyInfoChoiceRequest(
                괌대학_A_지원_정보.getId(),
                null,
                null
        );
        ApplyRequest request = new ApplyRequest(gpaScore.getId(), languageTestScore.getId(), univApplyInfoChoiceRequest);

        // when & then
        assertThatCode(() ->
                               applicationSubmissionService.apply(user.getId(), request)
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_GPA_SCORE_STATUS.getMessage());
    }

    @Test
    void 미승인된_어학성적으로_지원하면_예외가_발생한다() {
        // given
        GpaScore gpaScore = gpaScoreFixture.GPA_점수(VerifyStatus.APPROVED, user);
        LanguageTestScore languageTestScore = languageTestScoreFixture.어학_점수(VerifyStatus.PENDING, user);
        UnivApplyInfoChoiceRequest univApplyInfoChoiceRequest = new UnivApplyInfoChoiceRequest(
                괌대학_A_지원_정보.getId(),
                null,
                null
        );
        ApplyRequest request = new ApplyRequest(gpaScore.getId(), languageTestScore.getId(), univApplyInfoChoiceRequest);

        // when & then
        assertThatCode(() ->
                               applicationSubmissionService.apply(user.getId(), request)
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_LANGUAGE_TEST_SCORE_STATUS.getMessage());
    }

    @Test
    void 지원서_수정_횟수를_초과하면_예외가_발생한다() {
        // given
        GpaScore gpaScore = gpaScoreFixture.GPA_점수(VerifyStatus.APPROVED, user);
        LanguageTestScore languageTestScore = languageTestScoreFixture.어학_점수(VerifyStatus.APPROVED, user);
        UnivApplyInfoChoiceRequest univApplyInfoChoiceRequest = new UnivApplyInfoChoiceRequest(
                괌대학_A_지원_정보.getId(),
                null,
                null
        );
        ApplyRequest request = new ApplyRequest(gpaScore.getId(), languageTestScore.getId(), univApplyInfoChoiceRequest);

        for (int i = 0; i < APPLICATION_UPDATE_COUNT_LIMIT; i++) {
            applicationSubmissionService.apply(user.getId(), request);
        }

        // when & then
        assertThatCode(() ->
                               applicationSubmissionService.apply(user.getId(), request)
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(APPLY_UPDATE_LIMIT_EXCEED.getMessage());
    }
}
