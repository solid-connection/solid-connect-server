package com.example.solidconnection.unit.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.dto.ScoreRequest;
import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.application.service.ApplicationSubmissionService;
import com.example.solidconnection.custom.exception.CustomException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.SCORE_SHOULD_SUBMITTED_FIRST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    SiteUserRepository siteUserRepository;
    @Mock
    UniversityInfoForApplyRepository universityInfoForApplyRepository;

    private SiteUser siteUser;
    private Application application;
    private Application applicationBeforeTerm;

    private String term = "2024-1";
    private String beforeTerm = "1999-1";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(applicationSubmissionService, "term", term); // 테스트시 @value값 주입위함
        siteUser = createSiteUser();
        application = createApplication(term);
        applicationBeforeTerm = createApplication(beforeTerm);
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

    private Application createApplication(String term) {
        return new Application(
                siteUser,
                new Gpa(4.0, 4.5, "url"),
                new LanguageTest(LanguageTestType.TOEIC, "900", "url"),
                term
        );
    }

    @Test
    void 성적을_제출한다_금학기_제출이력_없음() {
        // Given
        ScoreRequest scoreRequest = new ScoreRequest(
                LanguageTestType.TOEIC, "990", "url", 4.5, 4.5, "url"
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(applicationRepository.findBySiteUserAndTerm(siteUser, term)).thenReturn(Optional.empty());

        // When
        applicationSubmissionService.submitScore(siteUser.getEmail(), scoreRequest);

        // Then
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
        verify(applicationRepository, times(1)).findBySiteUserAndTerm(siteUser, term);
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    void 성적을_제출한다_금학기_제출이력_있음() {
        // Given
        ScoreRequest scoreRequest = new ScoreRequest(
                LanguageTestType.TOEIC, "990", "url", 4.5, 4.5, "url"
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(applicationRepository.findBySiteUserAndTerm(siteUser, term)).thenReturn(Optional.of(application));

        // When
        applicationSubmissionService.submitScore(siteUser.getEmail(), scoreRequest);

        // Then
        assertEquals(application.getGpa().getGpa(), scoreRequest.gpa());
        assertEquals(application.getLanguageTest().getLanguageTestScore(), scoreRequest.languageTestScore());
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
        verify(applicationRepository, times(1)).findBySiteUserAndTerm(siteUser, term);
        verify(applicationRepository, times(0)).save(any(Application.class));
    }

    // 예외테스트
    @Test
    void 지망대학_제출할_때_성적_제출이력이_없다면_예외_응답을_반환한다() {
        // given
        UniversityChoiceRequest universityChoiceRequest = new UniversityChoiceRequest(
                1L, 2L, 3L
        );
        when(applicationRepository.findTop1BySiteUser_EmailOrderByTermDesc(siteUser.getEmail()))
                .thenReturn(Optional.empty());
        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> {
            applicationSubmissionService.submitUniversityChoice(siteUser.getEmail(), universityChoiceRequest);
        });
        assertThat(exception.getMessage())
                .isEqualTo(SCORE_SHOULD_SUBMITTED_FIRST.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(SCORE_SHOULD_SUBMITTED_FIRST.getCode());
    }

    @Test
    void 지망대학_제출한다_이전학기_성적_제출이력_있음() {
        // Given
        UniversityChoiceRequest universityChoiceRequest = new UniversityChoiceRequest(
                1L, 2L, 3L
        );
        when(applicationRepository.findTop1BySiteUser_EmailOrderByTermDesc(siteUser.getEmail()))
                .thenReturn(Optional.of(applicationBeforeTerm));
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);

        // When
        applicationSubmissionService.submitUniversityChoice(siteUser.getEmail(), universityChoiceRequest);

        // Then
        verify(applicationRepository, times(1)).findTop1BySiteUser_EmailOrderByTermDesc(siteUser.getEmail());
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    void 지망대학_제출한다_금학기_성적_제출이력_있음() {
        // Given
        UniversityChoiceRequest universityChoiceRequest = new UniversityChoiceRequest(
                1L, 2L, 3L
        );
        when(applicationRepository.findTop1BySiteUser_EmailOrderByTermDesc(siteUser.getEmail()))
                .thenReturn(Optional.of(application));

        // When
        applicationSubmissionService.submitUniversityChoice(siteUser.getEmail(), universityChoiceRequest);

        // Then
        verify(applicationRepository, times(1)).findTop1BySiteUser_EmailOrderByTermDesc(siteUser.getEmail());
        verify(siteUserRepository, times(0)).getByEmail(siteUser.getEmail());
        verify(applicationRepository, times(0)).save(any(Application.class));
    }
}
