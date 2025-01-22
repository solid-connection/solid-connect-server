package com.example.solidconnection.util;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Date;

import static com.example.solidconnection.util.JwtUtils.parseSubject;
import static com.example.solidconnection.util.JwtUtils.parseSubjectOrElseThrow;
import static com.example.solidconnection.util.JwtUtils.parseTokenFromRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("JwtUtils 테스트")
class JwtUtilsTest {

    private final String jwtSecretKey = "jwt-secret-key";

    @Nested
    class 요청으로부터_토큰을_추출한다 {

        @Test
        void 토큰이_있으면_토큰을_반환한다() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            String token = "token";
            request.addHeader("Authorization", "Bearer " + token);

            // when
            String extractedToken = parseTokenFromRequest(request);

            // then
            assertThat(extractedToken).isEqualTo(token);
        }

        @Test
        void 토큰이_없으면_null_을_반환한다() {
            // given
            MockHttpServletRequest noHeader = new MockHttpServletRequest();
            MockHttpServletRequest wrongPrefix = new MockHttpServletRequest();
            wrongPrefix.addHeader("Authorization", "Wrong token");
            MockHttpServletRequest emptyToken = new MockHttpServletRequest();
            wrongPrefix.addHeader("Authorization", "Bearer ");

            // when & then
            assertAll(
                    () -> assertThat(parseTokenFromRequest(noHeader)).isNull(),
                    () -> assertThat(parseTokenFromRequest(wrongPrefix)).isNull(),
                    () -> assertThat(parseTokenFromRequest(emptyToken)).isNull()
            );
        }
    }

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
        void 유효하지_않은_토큰의_subject_를_추출한다() {
            // given
            String subject = "subject999";
            String token = createInvalidToken(subject);

            // when
            String extractedSubject = parseSubject(token, jwtSecretKey);

            // then
            assertThat(extractedSubject).isEqualTo(subject);
        }

        @Test
        void 유효하지_않은_토큰의_subject_를_추출하면_예외_응답을_반환한다() {
            // given
            String subject = "subject123";
            String token = createInvalidToken(subject);

            // when
            assertThatCode(() -> parseSubjectOrElseThrow(token, jwtSecretKey))
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

    private String createInvalidToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();
    }
}
