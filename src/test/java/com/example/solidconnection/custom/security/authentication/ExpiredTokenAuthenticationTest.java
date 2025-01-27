package com.example.solidconnection.custom.security.authentication;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("만료된 토큰 인증 정보 테스트")
class ExpiredTokenAuthenticationTest {

    @Test
    void 인증_정보에_저장된_토큰을_반환한다() {
        // given
        String token = "token123";
        ExpiredTokenAuthentication auth = new ExpiredTokenAuthentication(token);

        // when
        String result = auth.getToken();

        // then
        assertThat(result).isEqualTo(token);
    }

    @Test
    void 인증_정보에_저장된_토큰의_subject_를_반환한다() {
        // given
        String subject = "subject321";
        String token = createToken(subject);
        ExpiredTokenAuthentication auth = new ExpiredTokenAuthentication(token, subject);

        // when
        String result = auth.getSubject();

        // then
        assertThat(result).isEqualTo(subject);
    }

    @Test
    void 항상_isAuthenticated_는_false_를_반환한다() {
        // given
        ExpiredTokenAuthentication auth1 = new ExpiredTokenAuthentication("token");
        ExpiredTokenAuthentication auth2 = new ExpiredTokenAuthentication("token", "subject");

        // when & then
        assertAll(
                () -> assertThat(auth1.isAuthenticated()).isFalse(),
                () -> assertThat(auth2.isAuthenticated()).isFalse()
        );
    }

    private String createToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000))
                .signWith(SignatureAlgorithm.HS256, "secret")
                .compact();
    }
}
