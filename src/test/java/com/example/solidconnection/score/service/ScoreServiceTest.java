package com.example.solidconnection.score.service;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.s3.S3Service;
import com.example.solidconnection.s3.UploadedFileUrlResponse;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.dto.GpaScoreRequest;
import com.example.solidconnection.score.dto.GpaScoreStatusResponse;
import com.example.solidconnection.score.dto.GpaScoreStatusesResponse;
import com.example.solidconnection.score.dto.LanguageTestScoreRequest;
import com.example.solidconnection.score.dto.LanguageTestScoreStatusResponse;
import com.example.solidconnection.score.dto.LanguageTestScoreStatusesResponse;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.s3.ImgType;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.type.VerifyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

@DisplayName("점수 서비스 테스트")
class ScoreServiceTest extends BaseIntegrationTest {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private GpaScoreRepository gpaScoreRepository;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private LanguageTestScoreRepository languageTestScoreRepository;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private SiteUserFixture siteUserFixture;

    private SiteUser user;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
    }

    @Test
    void GPA_점수_상태를_조회한다() {
        // given
        List<GpaScore> scores = List.of(
                createGpaScore(user, 3.5, 4.5),
                createGpaScore(user, 3.8, 4.5)
        );

        // when
        GpaScoreStatusesResponse response = scoreService.getGpaScoreStatus(user);

        // then
        assertThat(response.gpaScoreStatusResponseList())
                .hasSize(scores.size())
                .containsExactlyInAnyOrder(
                        scores.stream()
                                .map(GpaScoreStatusResponse::from)
                                .toArray(GpaScoreStatusResponse[]::new)
                );
    }

    @Test
    void GPA_점수가_없는_경우_빈_리스트를_반환한다() {
        // when
        GpaScoreStatusesResponse response = scoreService.getGpaScoreStatus(user);

        // then
        assertThat(response.gpaScoreStatusResponseList()).isEmpty();
    }

    @Test
    void 어학_시험_점수_상태를_조회한다() {
        // given
        List<LanguageTestScore> scores = List.of(
                createLanguageTestScore(user, LanguageTestType.TOEIC, "100"),
                createLanguageTestScore(user, LanguageTestType.TOEFL_IBT, "7.5")
        );
        siteUserRepository.save(user);

        // when
        LanguageTestScoreStatusesResponse response = scoreService.getLanguageTestScoreStatus(user);

        // then
        assertThat(response.languageTestScoreStatusResponseList())
                .hasSize(scores.size())
                .containsExactlyInAnyOrder(
                        scores.stream()
                                .map(LanguageTestScoreStatusResponse::from)
                                .toArray(LanguageTestScoreStatusResponse[]::new)
                );
    }

    @Test
    void 어학_시험_점수가_없는_경우_빈_리스트를_반환한다() {
        // when
        LanguageTestScoreStatusesResponse response = scoreService.getLanguageTestScoreStatus(user);

        // then
        assertThat(response.languageTestScoreStatusResponseList()).isEmpty();
    }

    @Test
    void GPA_점수를_등록한다() {
        // given
        GpaScoreRequest request = createGpaScoreRequest();
        MockMultipartFile file = createFile();
        String fileUrl = "/gpa-report.pdf";
        given(s3Service.uploadFile(file, ImgType.GPA)).willReturn(new UploadedFileUrlResponse(fileUrl));

        // when
        long scoreId = scoreService.submitGpaScore(user, request, file);
        GpaScore savedScore = gpaScoreRepository.findById(scoreId).orElseThrow();

        // then
        assertAll(
                () -> assertThat(savedScore.getId()).isEqualTo(scoreId),
                () -> assertThat(savedScore.getGpa().getGpa()).isEqualTo(request.gpa()),
                () -> assertThat(savedScore.getGpa().getGpaCriteria()).isEqualTo(request.gpaCriteria()),
                () -> assertThat(savedScore.getVerifyStatus()).isEqualTo(VerifyStatus.PENDING),
                () -> assertThat(savedScore.getGpa().getGpaReportUrl()).isEqualTo(fileUrl)
        );
    }

    @Test
    void 어학_시험_점수를_등록한다() {
        // given
        LanguageTestScoreRequest request = createLanguageTestScoreRequest();
        MockMultipartFile file = createFile();
        String fileUrl = "/gpa-report.pdf";
        given(s3Service.uploadFile(file, ImgType.LANGUAGE_TEST)).willReturn(new UploadedFileUrlResponse(fileUrl));

        // when
        long scoreId = scoreService.submitLanguageTestScore(user, request, file);
        LanguageTestScore savedScore = languageTestScoreRepository.findById(scoreId).orElseThrow();

        // then
        assertAll(
                () -> assertThat(savedScore.getId()).isEqualTo(scoreId),
                () -> assertThat(savedScore.getLanguageTest().getLanguageTestType()).isEqualTo(request.languageTestType()),
                () -> assertThat(savedScore.getLanguageTest().getLanguageTestScore()).isEqualTo(request.languageTestScore()),
                () -> assertThat(savedScore.getVerifyStatus()).isEqualTo(VerifyStatus.PENDING),
                () -> assertThat(savedScore.getLanguageTest().getLanguageTestReportUrl()).isEqualTo(fileUrl)
        );
    }

    private GpaScore createGpaScore(SiteUser siteUser, double gpa, double gpaCriteria) {
        GpaScore gpaScore = new GpaScore(
                new Gpa(gpa, gpaCriteria, "/gpa-report.pdf"),
                siteUser
        );
        gpaScore.setSiteUser(siteUser);
        return gpaScoreRepository.save(gpaScore);
    }

    private LanguageTestScore createLanguageTestScore(SiteUser siteUser, LanguageTestType languageTestType, String score) {
        LanguageTestScore languageTestScore = new LanguageTestScore(
                new LanguageTest(languageTestType, score, "/gpa-report.pdf"),
                siteUser
        );
        languageTestScore.setSiteUser(siteUser);
        return languageTestScoreRepository.save(languageTestScore);
    }

    private GpaScoreRequest createGpaScoreRequest() {
        return new GpaScoreRequest(
                3.5,
                4.5
        );
    }

    private LanguageTestScoreRequest createLanguageTestScoreRequest() {
        return new LanguageTestScoreRequest(
                LanguageTestType.TOEFL_IBT,
                "100"
        );
    }

    private MockMultipartFile createFile() {
        return new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }
}
