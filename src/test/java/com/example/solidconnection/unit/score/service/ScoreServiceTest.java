package com.example.solidconnection.unit.score.service;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.dto.*;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.score.service.ScoreService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("점수 서비스 테스트")
public class ScoreServiceTest {
    @InjectMocks
    ScoreService scoreService;
    @Mock
    GpaScoreRepository gpaScoreRepository;
    @Mock
    LanguageTestScoreRepository languageTestScoreRepository;
    @Mock
    SiteUserRepository siteUserRepository;

    private SiteUser siteUser;
    private GpaScore beforeGpaScore;
    private GpaScore beforeGpaScore2;
    private LanguageTestScore beforeLanguageTestScore;
    private LanguageTestScore beforeLanguageTestScore2;

    @BeforeEach
    void setUp() {
        siteUser = createSiteUser();
        beforeGpaScore = createBeforeGpaScore(siteUser, 4.5);
        beforeGpaScore2 = createBeforeGpaScore(siteUser, 4.3);
        beforeLanguageTestScore = createBeforeLanguageTestScore(siteUser);
        beforeLanguageTestScore2 = createBeforeLanguageTestScore2(siteUser);
    }

    private SiteUser createSiteUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
    }

    private GpaScore createBeforeGpaScore(SiteUser siteUser, Double gpa) {
        return new GpaScore(
                new Gpa(gpa, 4.5, "http://example.com/gpa-report.pdf"),
                siteUser,
                LocalDate.of(2024, 10, 20)
        );
    }

    private LanguageTestScore createBeforeLanguageTestScore(SiteUser siteUser) {
        return new LanguageTestScore(
                new LanguageTest(LanguageTestType.TOEIC, "900", "http://example.com/gpa-report.pdf"),
                LocalDate.of(2024, 10, 30),
                siteUser
        );
    }

    private LanguageTestScore createBeforeLanguageTestScore2(SiteUser siteUser) {
        return new LanguageTestScore(
                new LanguageTest(LanguageTestType.TOEFL_IBT, "100", "http://example.com/gpa-report.pdf"),
                LocalDate.of(2024, 10, 30),
                siteUser
        );
    }

    @Test
    void 학점을_등록한다_기존이력이_없을_때() {
        // Given
        GpaScoreRequest gpaScoreRequest = new GpaScoreRequest(
                4.5, 4.5, LocalDate.of(2024, 10, 20), "http://example.com/gpa-report.pdf"
        );
        GpaScore newGpaScore = new GpaScore(gpaScoreRequest.toGpa(), siteUser, gpaScoreRequest.issueDate());
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(gpaScoreRepository.save(newGpaScore)).thenReturn(newGpaScore);

        // 새로운 gpa 저장하게된다.
        scoreService.submitGpaScore(siteUser.getEmail(), gpaScoreRequest);

        // Then
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
        verify(gpaScoreRepository, times(1)).save(any(GpaScore.class));
    }

    @Test
    void 어학성적을_등록한다_기존이력이_없을_때() {
        // Given
        LanguageTestScoreRequest languageTestScoreRequest = new LanguageTestScoreRequest(
                LanguageTestType.TOEIC, "900",
                LocalDate.of(2024, 10, 30), "http://example.com/gpa-report.pdf"
        );
        LanguageTest languageTest = languageTestScoreRequest.toLanguageTest();
        LanguageTestScore languageTestScore = new LanguageTestScore(languageTest, LocalDate.of(2024, 10, 30), siteUser);

        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(languageTestScoreRepository.save(any(LanguageTestScore.class))).thenReturn(languageTestScore);

        //when
        scoreService.submitLanguageTestScore(siteUser.getEmail(), languageTestScoreRequest);

        // Then
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
        verify(languageTestScoreRepository, times(1)).save(any(LanguageTestScore.class));
    }

    @Test
    void 학점이력을_조회한다_제출이력이_있을_때() {
        // Given
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        beforeGpaScore.setSiteUser(siteUser);
        beforeGpaScore2.setSiteUser(siteUser);

        // when
        GpaScoreStatusResponse gpaScoreStatusResponse = scoreService.getGpaScoreStatus(siteUser.getEmail());

        // Then
        List<GpaScoreStatus> expectedStatusList = List.of(
                GpaScoreStatus.from(beforeGpaScore),
                GpaScoreStatus.from(beforeGpaScore2)
        );
        assertThat(gpaScoreStatusResponse.gpaScoreStatusList())
                .hasSize(2)
                .containsExactlyElementsOf(expectedStatusList);
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
    }

    @Test
    void 학점이력을_조회한다_제출이력이_없을_때() {
        // Given
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);

        // when
        GpaScoreStatusResponse gpaScoreStatus = scoreService.getGpaScoreStatus(siteUser.getEmail());

        // Then
        assertThat(gpaScoreStatus.gpaScoreStatusList()).isEmpty();
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
    }


    @Test
    void 어학이력을_조회한다_제출이력이_있을_때() {
        // Given
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        beforeLanguageTestScore.setSiteUser(siteUser);
        beforeLanguageTestScore2.setSiteUser(siteUser);

        // when
        LanguageTestScoreStatusResponse languageTestScoreStatus = scoreService.getLanguageTestScoreStatus(siteUser.getEmail());

        // Then
        List<LanguageTestScoreStatus> expectedStatusList = List.of(
                LanguageTestScoreStatus.from(beforeLanguageTestScore),
                LanguageTestScoreStatus.from(beforeLanguageTestScore2)
        );
        assertThat(languageTestScoreStatus.languageTestScoreStatusList())
                .hasSize(2)
                .containsExactlyElementsOf(expectedStatusList);
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
    }

    @Test
    void 어학이력을_조회한다_제출이력이_없을_때() {
        // Given
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);

        // when
        LanguageTestScoreStatusResponse languageTestScoreStatus = scoreService.getLanguageTestScoreStatus(siteUser.getEmail());

        // Then
        assertThat(languageTestScoreStatus.languageTestScoreStatusList()).isEmpty();
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
    }
}
