package com.example.solidconnection.siteuser.service;

import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_ALREADY_VERIFIED;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_ALREADY_USED;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_CONFIRM_CODE_DIFFERENT;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_CONFIRM_REQUEST_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_DOMAIN_NOT_SUPPORTED;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_VERIFICATION_INFO_CORRUPTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.mail.MailService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.fixture.HomeUniversityFixture;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestContainerSpringBootTest
@DisplayName("학교 이메일 인증 서비스 테스트")
class SchoolEmailServiceTest {

    @Autowired
    private SchoolEmailService schoolEmailService;

    @MockitoBean
    private MailService mailService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private HomeUniversityFixture homeUniversityFixture;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Nested
    @DisplayName("학교 이메일 인증 요청")
    class 학교_이메일_인증_요청 {

        @Test
        void 인증_코드가_발급되고_이메일이_발송된다() {
            // given
            homeUniversityFixture.인하대학교();
            SiteUser siteUser = siteUserFixture.사용자();

            // when & then
            schoolEmailService.requestSchoolEmailVerification(siteUser.getId(), " Test@INHA.EDU ");
            then(mailService).should().sendVerificationEmail(eq("test@inha.edu"), any());
        }

        @Test
        void 이미_학교_인증된_사용자는_예외가_발생한다() {
            // given
            HomeUniversity homeUniversity = homeUniversityFixture.인하대학교();
            SiteUser siteUser = siteUserFixture.국내_대학_정보_소지_사용자(homeUniversity.getId());

            // when & then
            assertThatThrownBy(() ->
                    schoolEmailService.requestSchoolEmailVerification(siteUser.getId(), "test@inha.edu"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(SCHOOL_EMAIL_ALREADY_VERIFIED.getMessage());
        }

        @Test
        void 이미_인증에_사용된_학교_이메일이면_예외가_발생한다() {
            // given
            homeUniversityFixture.인하대학교();
            SiteUser verifiedUser = siteUserFixture.사용자(1, "인증사용자");
            SiteUser anotherUser = siteUserFixture.사용자(2, "다른사용자");
            schoolEmailService.requestSchoolEmailVerification(verifiedUser.getId(), "test@inha.edu");

            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            then(mailService).should().sendVerificationEmail(eq("test@inha.edu"), codeCaptor.capture());
            schoolEmailService.confirmSchoolEmail(verifiedUser.getId(), codeCaptor.getValue());

            // when & then
            assertThatThrownBy(() ->
                    schoolEmailService.requestSchoolEmailVerification(anotherUser.getId(), "test@inha.edu"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(SCHOOL_EMAIL_ALREADY_USED.getMessage());
        }

        @Test
        void 지원하지_않는_이메일_도메인은_예외가_발생한다() {
            // given
            SiteUser siteUser = siteUserFixture.사용자();

            // when & then
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
            // given
            HomeUniversity homeUniversity = homeUniversityFixture.인하대학교();
            SiteUser siteUser = siteUserFixture.사용자();
            schoolEmailService.requestSchoolEmailVerification(siteUser.getId(), " Test@INHA.EDU ");

            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            then(mailService).should().sendVerificationEmail(any(), codeCaptor.capture());
            String code = codeCaptor.getValue();

            // When
            schoolEmailService.confirmSchoolEmail(siteUser.getId(), code);

            // Then
            SiteUser updated = siteUserRepository.findById(siteUser.getId()).orElseThrow();
            assertThat(updated.getHomeUniversityId()).isEqualTo(homeUniversity.getId());
            assertThat(updated.getVerifiedSchoolEmail()).isEqualTo("test@inha.edu");
        }

        @Test
        void 인증_정보가_없으면_예외가_발생한다() {
            // given
            SiteUser siteUser = siteUserFixture.사용자();

            // when & then
            assertThatThrownBy(() ->
                    schoolEmailService.confirmSchoolEmail(siteUser.getId(), "123456"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(SCHOOL_EMAIL_CONFIRM_REQUEST_NOT_FOUND.getMessage());
        }

        @Test
        void 인증_코드가_다르면_예외가_발생한다() {
            // given
            homeUniversityFixture.인하대학교();
            SiteUser siteUser = siteUserFixture.사용자();
            schoolEmailService.requestSchoolEmailVerification(siteUser.getId(), "test@inha.edu");

            // when & then
            assertThatThrownBy(() ->
                    schoolEmailService.confirmSchoolEmail(siteUser.getId(), "000000"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(SCHOOL_EMAIL_CONFIRM_CODE_DIFFERENT.getMessage());
        }

        @Test
        void 인증_정보가_손상되면_예외가_발생하고_인증_정보가_삭제된다() {
            // given
            HomeUniversity homeUniversity = homeUniversityFixture.인하대학교();
            SiteUser siteUser = siteUserFixture.사용자();
            String key = "school-email:" + siteUser.getId();
            redisTemplate.opsForValue().set(
                    key,
                    "{\"homeUniversityId\":" + homeUniversity.getId() + ",\"code\":\"123456\"}"
            );

            // when & then
            assertThatThrownBy(() ->
                    schoolEmailService.confirmSchoolEmail(siteUser.getId(), "123456"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(SCHOOL_EMAIL_VERIFICATION_INFO_CORRUPTED.getMessage());
            assertThat(redisTemplate.opsForValue().get(key)).isNull();
        }

        @Test
        void 같은_학교_이메일로_인증_코드를_받았더라도_먼저_인증한_사용자가_있으면_나중_인증은_예외가_발생한다() {
            // given
            homeUniversityFixture.인하대학교();
            SiteUser firstUser = siteUserFixture.사용자(1, "첫번째사용자");
            SiteUser secondUser = siteUserFixture.사용자(2, "두번째사용자");
            schoolEmailService.requestSchoolEmailVerification(firstUser.getId(), "test@inha.edu");
            schoolEmailService.requestSchoolEmailVerification(secondUser.getId(), "test@inha.edu");

            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            then(mailService).should(times(2)).sendVerificationEmail(eq("test@inha.edu"), codeCaptor.capture());
            List<String> codes = codeCaptor.getAllValues();
            schoolEmailService.confirmSchoolEmail(firstUser.getId(), codes.get(0));

            // when & then
            assertThatThrownBy(() ->
                    schoolEmailService.confirmSchoolEmail(secondUser.getId(), codes.get(1)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(SCHOOL_EMAIL_ALREADY_USED.getMessage());
        }
    }
}
