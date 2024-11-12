package com.example.solidconnection.unit.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.dto.ApplyRequest;
import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.application.service.ApplicationSubmissionService;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.*;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("지원 서비스 테스트")
public class ApplicationServiceTest {
    @InjectMocks
    ApplicationSubmissionService applicationSubmissionService;
    @Mock
    ApplicationRepository applicationRepository;
    @Mock
    UniversityInfoForApplyRepository universityInfoForApplyRepository;
    @Mock
    SiteUserRepository siteUserRepository;
    @Mock
    GpaScoreRepository gpaScoreRepository;
    @Mock
    LanguageTestScoreRepository languageTestScoreRepository;

    @Value("${university.term}")
    private String term;
    private SiteUser siteUser;
    private GpaScore gpaScore;
    private LanguageTestScore languageTestScore;
    private final long gpaScoreId = 1L;
    private final long languageTestScoreId = 1L;
    private final long firstChoiceUniversityId = 1L;
    private final long secondChoiceUniversityId = 2L;
    private final long thirdChoiceUniversityId = 3L;

    @BeforeEach
    void setUp() {
        siteUser = new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
        gpaScore = new GpaScore(
                new Gpa(4.3, 4.5, "gpaScoreUrl"),
                siteUser,
                LocalDate.of(2024, 10, 30)
        );
        languageTestScore = new LanguageTestScore(
                new LanguageTest(LanguageTestType.TOEIC, "990", "languageTestScoreUrl"),
                LocalDate.of(2024, 10, 30),
                siteUser
        );
    }

    @Test
    void 지원한다_기존_이력_없음() {
        // Given
        ApplyRequest applyRequest = new ApplyRequest(
                gpaScoreId,
                languageTestScoreId,
                new UniversityChoiceRequest(firstChoiceUniversityId, secondChoiceUniversityId, thirdChoiceUniversityId)
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        gpaScore.setVerifyStatus(VerifyStatus.APPROVED);
        when(gpaScoreRepository.findGpaScoreBySiteUserAndId(siteUser, gpaScoreId)).thenReturn(Optional.of(gpaScore));
        languageTestScore.setVerifyStatus(VerifyStatus.APPROVED);
        when(languageTestScoreRepository.findLanguageTestScoreBySiteUserAndId(siteUser, languageTestScoreId)).thenReturn(Optional.of(languageTestScore));
        when(applicationRepository.findBySiteUserAndTerm(siteUser, term)).thenReturn(Optional.empty());

        // When
        boolean result = applicationSubmissionService.apply(siteUser.getEmail(), applyRequest);

        // Then
        assertThat(result).isEqualTo(true);
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
        verify(gpaScoreRepository, times(1)).findGpaScoreBySiteUserAndId(siteUser, gpaScoreId);
        verify(languageTestScoreRepository, times(1)).findLanguageTestScoreBySiteUserAndId(siteUser, languageTestScoreId);
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    void 지원한다_기존_이력_있음() {
        // Given
        Application beforeApplication = new Application(
                siteUser,
                new Gpa(4.5, 4.5, "beforeGpaScoreUrl"),
                new LanguageTest(LanguageTestType.TOEIC, "900", "beforeLanguageTestUrl"),
                term
        );
        beforeApplication.setVerifyStatus(VerifyStatus.APPROVED);
        ApplyRequest applyRequest = new ApplyRequest(
                gpaScoreId,
                languageTestScoreId,
                new UniversityChoiceRequest(firstChoiceUniversityId, secondChoiceUniversityId, thirdChoiceUniversityId)
        );

        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        gpaScore.setVerifyStatus(VerifyStatus.APPROVED);
        when(gpaScoreRepository.findGpaScoreBySiteUserAndId(siteUser, 1L)).thenReturn(Optional.of(gpaScore));
        languageTestScore.setVerifyStatus(VerifyStatus.APPROVED);
        when(languageTestScoreRepository.findLanguageTestScoreBySiteUserAndId(siteUser, 1L)).thenReturn(Optional.of(languageTestScore));
        when(applicationRepository.findBySiteUserAndTerm(siteUser, term)).thenReturn(Optional.of(beforeApplication));

        // When
        boolean result = applicationSubmissionService.apply(siteUser.getEmail(), applyRequest);

        // Then
        assertThat(result).isEqualTo(true);
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
        verify(gpaScoreRepository, times(1)).findGpaScoreBySiteUserAndId(siteUser, gpaScoreId);
        verify(languageTestScoreRepository, times(1)).findLanguageTestScoreBySiteUserAndId(siteUser, languageTestScoreId);
        verify(applicationRepository, times(1)).findBySiteUserAndTerm(siteUser, term);
        verify(universityInfoForApplyRepository, times(1)).getUniversityInfoForApplyByIdAndTerm(firstChoiceUniversityId, term);
        verify(universityInfoForApplyRepository, times(1)).getUniversityInfoForApplyByIdAndTerm(secondChoiceUniversityId, term);
        verify(universityInfoForApplyRepository, times(1)).getUniversityInfoForApplyByIdAndTerm(thirdChoiceUniversityId, term);
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    void 지원할_때_존재하지_않는_학점이라면_예외_응답을_반환한다() {
        // given
        ApplyRequest applyRequest = new ApplyRequest(
                gpaScoreId,
                languageTestScoreId,
                new UniversityChoiceRequest(firstChoiceUniversityId, secondChoiceUniversityId, thirdChoiceUniversityId)
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(gpaScoreRepository.findGpaScoreBySiteUserAndId(siteUser, gpaScoreId)).thenReturn(Optional.empty());
        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> {
            applicationSubmissionService.apply(siteUser.getEmail(), applyRequest);
        });
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_GPA_SCORE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_GPA_SCORE.getCode());
    }

    @Test
    void 지원할_때_승인되지_않은_학점이라면_예외_응답을_반환한다() {
        // given
        ApplyRequest applyRequest = new ApplyRequest(
                gpaScoreId,
                languageTestScoreId,
                new UniversityChoiceRequest(firstChoiceUniversityId, secondChoiceUniversityId, thirdChoiceUniversityId)
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        gpaScore.setVerifyStatus(VerifyStatus.REJECTED);
        when(gpaScoreRepository.findGpaScoreBySiteUserAndId(siteUser, gpaScoreId)).thenReturn(Optional.of(gpaScore));

        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> {
            applicationSubmissionService.apply(siteUser.getEmail(), applyRequest);
        });
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_GPA_SCORE_STATUS.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_GPA_SCORE_STATUS.getCode());
    }

    @Test
    void 지원할_때_존재하지_않는_어학성적이라면_예외_응답을_반환한다() {
        // given
        ApplyRequest applyRequest = new ApplyRequest(
                gpaScoreId,
                languageTestScoreId,
                new UniversityChoiceRequest(firstChoiceUniversityId, secondChoiceUniversityId, thirdChoiceUniversityId)
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        gpaScore.setVerifyStatus(VerifyStatus.APPROVED);
        when(gpaScoreRepository.findGpaScoreBySiteUserAndId(siteUser, gpaScoreId)).thenReturn(Optional.of(gpaScore));
        when(languageTestScoreRepository.findLanguageTestScoreBySiteUserAndId(siteUser, languageTestScoreId)).thenReturn(Optional.empty());

        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> {
            applicationSubmissionService.apply(siteUser.getEmail(), applyRequest);
        });
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_LANGUAGE_TEST_SCORE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_LANGUAGE_TEST_SCORE.getCode());
    }

    @Test
    void 지원할_때_승인되지_않은_어학성적이라면_예외_응답을_반환한다() {
        // given
        ApplyRequest applyRequest = new ApplyRequest(
                gpaScoreId,
                languageTestScoreId,
                new UniversityChoiceRequest(firstChoiceUniversityId, secondChoiceUniversityId, thirdChoiceUniversityId)
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        gpaScore.setVerifyStatus(VerifyStatus.APPROVED);
        when(gpaScoreRepository.findGpaScoreBySiteUserAndId(siteUser, gpaScoreId)).thenReturn(Optional.of(gpaScore));
        languageTestScore.setVerifyStatus(VerifyStatus.REJECTED);
        when(languageTestScoreRepository.findLanguageTestScoreBySiteUserAndId(siteUser, languageTestScoreId)).thenReturn(Optional.of(languageTestScore));

        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> {
            applicationSubmissionService.apply(siteUser.getEmail(), applyRequest);
        });
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_LANGUAGE_TEST_SCORE_STATUS.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_LANGUAGE_TEST_SCORE_STATUS.getCode());
    }

    @Test
    void 지원할_때_학교_선택이_중복되면_예외_응답을_반환한다() {
        // given
        ApplyRequest applyRequest = new ApplyRequest(
                gpaScoreId,
                languageTestScoreId,
                new UniversityChoiceRequest(firstChoiceUniversityId, firstChoiceUniversityId, firstChoiceUniversityId)
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);

        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> {
            applicationSubmissionService.apply(siteUser.getEmail(), applyRequest);
        });
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.CANT_APPLY_FOR_SAME_UNIVERSITY.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.CANT_APPLY_FOR_SAME_UNIVERSITY.getCode());
    }
}
