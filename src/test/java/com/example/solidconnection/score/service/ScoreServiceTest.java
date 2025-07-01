package com.example.solidconnection.score.service;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.dto.GpaScoreRequest;
import com.example.solidconnection.score.dto.GpaScoreStatusesResponse;
import com.example.solidconnection.score.dto.LanguageTestScoreRequest;
import com.example.solidconnection.score.dto.LanguageTestScoreStatusesResponse;
import com.example.solidconnection.score.fixture.GpaScoreFixture;
import com.example.solidconnection.score.fixture.LanguageTestScoreFixture;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.LanguageTestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@TestContainerSpringBootTest
@DisplayName("점수 서비스 테스트")
class ScoreServiceTest {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private GpaScoreRepository gpaScoreRepository;

    @Autowired
    private LanguageTestScoreRepository languageTestScoreRepository;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private GpaScoreFixture gpaScoreFixture;

    @Autowired
    private LanguageTestScoreFixture languageTestScoreFixture;

    private SiteUser user;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
    }

    @Test
    void GPA_점수_상태를_조회한다() {
        // given
        List<GpaScore> scores = List.of(
                gpaScoreFixture.GPA_점수(VerifyStatus.PENDING, user),
                gpaScoreFixture.GPA_점수(VerifyStatus.APPROVED, user)
        );

        // when
        GpaScoreStatusesResponse response = scoreService.getGpaScoreStatus(user);

        // then
        assertThat(response.gpaScoreStatusResponseList()).hasSize(scores.size());
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
                languageTestScoreFixture.어학_점수(VerifyStatus.PENDING, user),
                languageTestScoreFixture.어학_점수(VerifyStatus.PENDING, user)
        );

        // when
        LanguageTestScoreStatusesResponse response = scoreService.getLanguageTestScoreStatus(user);

        // then
        assertThat(response.languageTestScoreStatusResponseList()).hasSize(scores.size());
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
        assertThat(savedScore.getId()).isEqualTo(scoreId);
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
        assertThat(savedScore.getId()).isEqualTo(scoreId);
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
