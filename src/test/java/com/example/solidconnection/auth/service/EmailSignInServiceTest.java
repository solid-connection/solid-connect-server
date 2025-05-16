package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.EmailSignInRequest;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.siteuser.domain.PreparationStatus;
import com.example.solidconnection.siteuser.domain.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("이메일 로그인 서비스 테스트")
@TestContainerSpringBootTest
class EmailSignInServiceTest {

    @Autowired
    private EmailSignInService emailSignInService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Test
    void 로그인에_성공한다() {
        // given
        String email = "testEmail";
        String rawPassword = "testPassword";
        SiteUser user = siteUserFixture.사용자(email, rawPassword);
        EmailSignInRequest signInRequest = new EmailSignInRequest(user.getEmail(), rawPassword);

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
            siteUserFixture.사용자(email, "testPassword");
            EmailSignInRequest signInRequest = new EmailSignInRequest(email, "틀린비밀번호");

            // when & then
            assertThatCode(() -> emailSignInService.signIn(signInRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
        }
    }
}
