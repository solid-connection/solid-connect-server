package com.example.solidconnection.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import com.example.solidconnection.auth.controller.config.RefreshTokenCookieProperties;
import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("리프레시 토큰 쿠키 매니저 테스트")
@TestContainerSpringBootTest
class RefreshTokenCookieManagerTest {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @Autowired
    private RefreshTokenCookieManager cookieManager;

    @MockBean
    private RefreshTokenCookieProperties refreshTokenCookieProperties;

    private final String sameSite = "Strict";
    private final String domain = "example.com";

    @BeforeEach
    void setUp() {
        given(refreshTokenCookieProperties.cookieDomain()).willReturn(domain);
        given(refreshTokenCookieProperties.sameSite()).willReturn(sameSite);
    }

    @Test
    void 리프레시_토큰을_쿠키로_설정한다() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        String refreshToken = "test-refresh-token";

        // when
        cookieManager.setCookie(response, refreshToken);

        // then
        String header = response.getHeader("Set-Cookie");
        assertAll(
                () -> assertThat(header).isNotNull(),
                () -> assertThat(header).contains(REFRESH_TOKEN_COOKIE_NAME + "=" + refreshToken),
                () -> assertThat(header).contains("HttpOnly"),
                () -> assertThat(header).contains("Secure"),
                () -> assertThat(header).contains("Path=/"),
                () -> assertThat(header).contains("Max-Age=" + TokenType.REFRESH.getExpireTime() / 1000),
                () -> assertThat(header).contains("Domain=" + domain),
                () -> assertThat(header).contains("SameSite=" + sameSite)
        );
    }

    @Test
    void 쿠키에서_리프레시_토큰을_삭제한다() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        cookieManager.deleteCookie(response);

        // then
        String header = response.getHeader("Set-Cookie");
        assertAll(
                () -> assertThat(header).isNotNull(),
                () -> assertThat(header).contains(REFRESH_TOKEN_COOKIE_NAME + "="),
                () -> assertThat(header).contains("HttpOnly"),
                () -> assertThat(header).contains("Secure"),
                () -> assertThat(header).contains("Path=/"),
                () -> assertThat(header).contains("Max-Age=0"),
                () -> assertThat(header).contains("Domain=" + domain),
                () -> assertThat(header).contains("SameSite=" + sameSite)
        );
    }

    @Nested
    class 쿠키에서_리프레시_토큰을_추출한다 {

        @Test
        void 리프레시_토큰이_있으면_정상_반환한다() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            String refreshToken = "test-refresh-token";
            request.setCookies(new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken));

            // when
            String retrievedToken = cookieManager.getRefreshToken(request);

            // then
            assertThat(retrievedToken).isEqualTo(refreshToken);
        }

        @Test
        void 쿠키가_없으면_예외가_발생한다() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();

            // when & then
            assertThatCode(() -> cookieManager.getRefreshToken(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.REFRESH_TOKEN_NOT_EXISTS.getMessage());
        }

        @Test
        void 리프레시_토큰_쿠키가_없으면_예외가_발생한다() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie("otherCookie", "some-value"));

            // when & then
            assertThatCode(() -> cookieManager.getRefreshToken(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.REFRESH_TOKEN_NOT_EXISTS.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void 리프레시_토큰_쿠키가_비어있으면_예외가_발생한다(String token) {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie(REFRESH_TOKEN_COOKIE_NAME, token));

            // when & then
            assertThatCode(() -> cookieManager.getRefreshToken(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.REFRESH_TOKEN_NOT_EXISTS.getMessage());
        }
    }
}
