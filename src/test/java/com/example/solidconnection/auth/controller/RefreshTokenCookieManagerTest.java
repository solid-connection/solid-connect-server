package com.example.solidconnection.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import com.example.solidconnection.auth.controller.config.RefreshTokenCookieProperties;
import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("리프레시 토큰 쿠키 매니저 테스트")
@TestContainerSpringBootTest
class RefreshTokenCookieManagerTest {

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
                () -> assertThat(header).contains("refreshToken=" + refreshToken),
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
                () -> assertThat(header).contains("refreshToken="),
                () -> assertThat(header).contains("HttpOnly"),
                () -> assertThat(header).contains("Secure"),
                () -> assertThat(header).contains("Path=/"),
                () -> assertThat(header).contains("Max-Age=0"),
                () -> assertThat(header).contains("SameSite=Strict"),
                () -> assertThat(header).contains("Domain=" + domain),
                () -> assertThat(header).contains("SameSite=" + sameSite)
        );
    }
}
