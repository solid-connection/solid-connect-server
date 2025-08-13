package com.example.solidconnection.auth.controller.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.Cookie.SameSite;

@DisplayName("리프레시 토큰 쿠키 설정 테스트")
class RefreshTokenCookiePropertiesTest {

    @Test
    void Domain을_지정했으면_SameSite가_Strict() {
        // given
        RefreshTokenCookieProperties properties = new RefreshTokenCookieProperties("example.com");

        // when
        String sameSite = properties.sameSite();

        // then
        assertThat(sameSite).isEqualTo(SameSite.STRICT.attributeValue());
    }

    @Test
    void Domain을_지정하지_않았으면_SameSite가_None() {
        // given
        RefreshTokenCookieProperties properties = new RefreshTokenCookieProperties(null);

        // when
        String sameSite = properties.sameSite();

        // then
        assertThat(sameSite).isEqualTo(SameSite.NONE.attributeValue());
    }
}
