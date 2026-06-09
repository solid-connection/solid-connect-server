package com.example.solidconnection.siteuser.service;

import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_ALREADY_VERIFIED;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_CONFIRM_CODE_DIFFERENT;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_CONFIRM_REQUEST_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_DOMAIN_NOT_SUPPORTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.email.service.EmailService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.fixture.HomeUniversityFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestContainerSpringBootTest
@DisplayName("학교 이메일 인증 서비스 테스트")
class SchoolEmailServiceTest {

    @Autowired
    private SchoolEmailService schoolEmailService;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private HomeUniversityFixture homeUniversityFixture;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Nested
    @DisplayName("학교 이메일 인증 요청")
    class 학교_이메일_인증_요청 {

        @Test
        void 인증_코드가_발급되고_이메일이_발송된다() {
            // Given
            homeUniversityFixture.인천대학교();
            SiteUser siteUser = siteUserFixture.사용자();

            // When
            schoolEmailService.requestSchoolEmailVerification(siteUser.getId(), "test@inu.ac.kr");

            // Then
            then(emailService).should().sendVerificationEmail(eq("test@inu.ac.kr"), any());
        }

        @Test
        void 이미_학교_인증된_사용자는_예외가_발생한다() {
            // Given
            HomeUniversity homeUniversity = homeUniversityFixture.인천대학교();
            SiteUser siteUser = siteUserFixture.국내_대학_정보_소지_사용자(homeUniversity.getId());

            // When & Then
            assertThatThrownBy(() ->
                    schoolEmailService.requestSchoolEmailVerification(siteUser.getId(), "test@inu.ac.kr"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(SCHOOL_EMAIL_ALREADY_VERIFIED.getMessage());
        }

        @Test
        void 지원하지_않는_이메일_도메인은_예외가_발생한다() {
            // Given
            SiteUser siteUser = siteUserFixture.사용자();

            // When & Then
            assertThatThrownBy(() ->
                    schoolEmailService.requestSchoolEmailVerification(siteUser.getId(), "test@unknown.ac.kr"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(SCHOOL_EMAIL_DOMAIN_NOT_SUPPORTED.getMessage());
        }
    }

    @Nested
    @DisplayName("학교 이메일 인증 확인")
    class 학교_이메일_인증_확인 {

        @Test
        void 인증_코드가_일치하면_homeUniversityId가_설정되고_인증이_완료된다() {
            // Given
            HomeUniversity homeUniversity = homeUniversityFixture.인천대학교();
            SiteUser siteUser = siteUserFixture.사용자();
            schoolEmailService.requestSchoolEmailVerification(siteUser.getId(), "test@inu.ac.kr");

            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            then(emailService).should().sendVerificationEmail(any(), codeCaptor.capture());
            String code = codeCaptor.getValue();

            // When
            schoolEmailService.confirmSchoolEmail(siteUser.getId(), code);

            // Then
            SiteUser updated = siteUserRepository.findById(siteUser.getId()).orElseThrow();
            assertThat(updated.getHomeUniversityId()).isEqualTo(homeUniversity.getId());
        }

        @Test
        void 인증_요청이_없으면_예외가_발생한다() {
            // Given
            SiteUser siteUser = siteUserFixture.사용자();

            // When & Then
            assertThatThrownBy(() ->
                    schoolEmailService.confirmSchoolEmail(siteUser.getId(), "123456"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(SCHOOL_EMAIL_CONFIRM_REQUEST_NOT_FOUND.getMessage());
        }

        @Test
        void 인증_코드가_다르면_예외가_발생한다() {
            // Given
            homeUniversityFixture.인천대학교();
            SiteUser siteUser = siteUserFixture.사용자();
            schoolEmailService.requestSchoolEmailVerification(siteUser.getId(), "test@inu.ac.kr");

            // When & Then
            assertThatThrownBy(() ->
                    schoolEmailService.confirmSchoolEmail(siteUser.getId(), "000000"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(SCHOOL_EMAIL_CONFIRM_CODE_DIFFERENT.getMessage());
        }
    }
}
