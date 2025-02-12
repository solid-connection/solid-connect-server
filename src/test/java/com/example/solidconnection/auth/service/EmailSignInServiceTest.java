package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.EmailSignInRequest;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("이메일 로그인 서비스 테스트")
@TestContainerSpringBootTest
class EmailSignInServiceTest {

    @Autowired
    private EmailSignInService emailSignInService;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 로그인에_성공한다() {
        // given
        String email = "testEmail";
        String rawPassword = "testPassword";
        SiteUser siteUser = createSiteUser(email, rawPassword);
        siteUserRepository.save(siteUser);
        EmailSignInRequest signInRequest = new EmailSignInRequest(siteUser.getEmail(), rawPassword);

        // when
        SignInResponse signInResponse = emailSignInService.signIn(signInRequest);

        // then
        assertAll(
                () -> Assertions.assertThat(signInResponse.accessToken()).isNotNull(),
                () -> Assertions.assertThat(signInResponse.refreshToken()).isNotNull()
        );
    }

    @Nested
    class 로그인에_실패한다 {

        @Test
        void 이메일과_일치하는_사용자가_없으면_예외_응답을_반환한다() {
            // given
            EmailSignInRequest signInRequest = new EmailSignInRequest("이메일", "비밀번호");

            // when & then
            assertThatCode(() -> emailSignInService.signIn(signInRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        void 비밀번호가_일치하지_않으면_예외_응답을_반환한다() {
            // given
            String email = "testEmail";
            SiteUser siteUser = createSiteUser(email, "testPassword");
            siteUserRepository.save(siteUser);
            EmailSignInRequest signInRequest = new EmailSignInRequest(email, "틀린비밀번호");

            // when & then
            assertThatCode(() -> emailSignInService.signIn(signInRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
        }
    }

    private SiteUser createSiteUser(String email, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        return new SiteUser(
                email,
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE,
                AuthType.EMAIL,
                encodedPassword
        );
    }
}
