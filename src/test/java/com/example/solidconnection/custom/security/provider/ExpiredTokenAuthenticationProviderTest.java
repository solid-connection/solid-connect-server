package com.example.solidconnection.custom.security.provider;

import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.security.authentication.ExpiredTokenAuthentication;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.net.PasswordAuthentication;
import java.util.Date;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.*;

@TestContainerSpringBootTest
@DisplayName("만료된 토큰 provider 테스트")
class ExpiredTokenAuthenticationProviderTest {

    @Autowired
    private ExpiredTokenAuthenticationProvider expiredTokenAuthenticationProvider;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void 처리할_수_있는_타입인지를_반환한다() {
        // given
        Class<?> supportedType = ExpiredTokenAuthentication.class;
        Class<?> notSupportedType = PasswordAuthentication.class;

        // when & then
        assertAll(
                () -> assertTrue(expiredTokenAuthenticationProvider.supports(supportedType)),
                () -> assertFalse(expiredTokenAuthenticationProvider.supports(notSupportedType))
        );
    }

    @Test
    void 만료된_토큰의_인증_정보를_반환한다() {
        // given
        String expiredToken = createExpiredToken();
        ExpiredTokenAuthentication ExpiredTokenAuthentication = new ExpiredTokenAuthentication(expiredToken);

        // when
        Authentication result = expiredTokenAuthenticationProvider.authenticate(ExpiredTokenAuthentication);

        // then
        assertAll(
                () -> assertThat(result).isInstanceOf(ExpiredTokenAuthentication.class),
                () -> assertThat(result.isAuthenticated()).isFalse()
        );
    }

    @Test
    void 유효하지_않은_토큰이면_예외_응답을_반환한다() {
        // given
        ExpiredTokenAuthentication ExpiredTokenAuthentication = new ExpiredTokenAuthentication("invalid token");

        // when & then
        assertThatCode(() -> expiredTokenAuthenticationProvider.authenticate(ExpiredTokenAuthentication))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(INVALID_TOKEN.getMessage());
    }

    private String createExpiredToken() {
        return Jwts.builder()
                .setSubject("1")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
    }
}
