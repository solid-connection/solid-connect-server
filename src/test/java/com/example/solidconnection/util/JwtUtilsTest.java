package com.example.solidconnection.util;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static com.example.solidconnection.util.JwtUtils.parseSubject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("JwtUtils 테스트")
class JwtUtilsTest {

    private final String jwtSecretKey = "jwt-secret-key";

    @Nested
    class 토큰으로부터_subject_를_추출한다 {

        @Test
        void 유효한_토큰의_subject_를_추출한다() {
            // given
            String subject = "subject000";
            String token = createValidToken(subject);

            // when
            String extractedSubject = parseSubject(token, jwtSecretKey);

            // then
            assertThat(extractedSubject).isEqualTo(subject);
        }

        @Test
        void 유효하지_않은_토큰의_subject_를_추출하면_예외_응답을_반환한다() {
            // given
            String subject = "subject123";
            String token = createExpiredToken(subject);

            // when
            assertThatCode(() -> parseSubject(token, jwtSecretKey))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
        }
    }

    private String createValidToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();
    }

    private String createExpiredToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();
    }
}
