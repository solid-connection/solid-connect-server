package com.example.solidconnection.config.token;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
@DisplayName("TokenProvider 테스트")
class TokenProviderTest {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    private String secretKey;

    private final String subject = "subject";

    @Test
    void 토큰을_생성한다() {
        // when
        String token = tokenProvider.generateToken(subject, TokenType.ACCESS);

        // then
        String extractedSubject = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        assertThat(subject).isEqualTo(extractedSubject);
    }

    @Nested
    class 토큰을_저장한다 {

        @Test
        void 토큰이_유효하면_저장한다() {
            // given
            String token = createValidToken(subject);

            // when
            tokenProvider.saveToken(token, TokenType.ACCESS);

            // then
            String savedToken = redisTemplate.opsForValue().get(TokenType.ACCESS.addPrefixToSubject(subject));
            assertThat(savedToken).isEqualTo(token);
        }

        @Test
        void 토큰이_유효하지않으면_예외가_발생한다() {
            // given
            String token = createInvalidToken(subject);

            // when & then
            assertThatCode(() -> tokenProvider.saveToken(token, TokenType.REFRESH))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
        }
    }

    @Nested
    class 요청으로부터_토큰을_추출한다 {

        @Test
        void 토큰이_있으면_토큰을_반환한다() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            String token = "token";
            request.addHeader("Authorization", "Bearer " + token);

            // when
            String extractedToken = tokenProvider.parseTokenFromRequest(request);

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
                    () -> assertThat(tokenProvider.parseTokenFromRequest(noHeader)).isNull(),
                    () -> assertThat(tokenProvider.parseTokenFromRequest(wrongPrefix)).isNull(),
                    () -> assertThat(tokenProvider.parseTokenFromRequest(emptyToken)).isNull()
            );
        }
    }

    @Nested
    class 토큰으로부터_subject_를_추출한다 {

        @Test
        void 유효한_토큰의_subject_를_추출한다() {
            // given
            String token = createValidToken(subject);

            // when
            String extractedSubject = tokenProvider.parseSubject(token);

            // then
            assertThat(extractedSubject).isEqualTo(subject);
        }

        @Test
        void 유효하지_않은_토큰의_subject_를_추출한다() {
            // given
            String token = createInvalidToken(subject);

            // when
            String extractedSubject = tokenProvider.parseSubject(token);

            // then
            assertThat(extractedSubject).isEqualTo(subject);
        }

        @Test
        void 유효하지_않은_토큰의_subject_를_추출하면_예외가_발생한다() {
            // given
            String token = createInvalidToken(subject);

            // when
            assertThatCode(() -> tokenProvider.parseSubjectOrElseThrow(token))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
        }

    }

    private String createValidToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    private String createInvalidToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}
