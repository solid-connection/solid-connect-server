package com.example.solidconnection.auth.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.domain.Subject;
import com.example.solidconnection.auth.domain.Token;
import com.example.solidconnection.auth.service.TokenProvider;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("Redis 토큰 저장소 테스트")
class RedisTokenStorageTest {

    @Autowired
    private RedisTokenStorage redisTokenStorage;

    @Autowired
    private TokenProvider tokenProvider;

    private Subject subject;
    private Token expectedToken;
    private Class<? extends Token> tokenClass;

    @BeforeEach
    void setUp() {
        subject = new Subject("subject123");
        expectedToken = new AccessToken(tokenProvider.generateToken(subject, Duration.ofMinutes(10)));
        tokenClass = expectedToken.getClass();
    }

    @Test
    void 토큰을_저장한다() {
        // when
        Token savedToken = redisTokenStorage.saveToken(subject, expectedToken);

        // then
        Optional<String> foundToken = redisTokenStorage.findToken(subject, tokenClass);
        assertAll(
                () -> assertThat(foundToken).hasValue(expectedToken.token()),
                () -> assertThat(savedToken).isEqualTo(expectedToken)
        );
    }

    @Nested
    class 토큰을_조회한다 {

        @Test
        void 저장된_토큰이_있으면_Optional에_담아_반한다() {
            // given
            redisTokenStorage.saveToken(subject, expectedToken);

            // when
            Optional<String> actualToken = redisTokenStorage.findToken(subject, tokenClass);

            // then
            assertThat(actualToken).hasValue(expectedToken.token());
        }

        @Test
        void 저장된_토큰이_없으면_빈_Optional을_반환한다() {
            // when
            Optional<String> foundToken = redisTokenStorage.findToken(subject, tokenClass);

            // then
            assertThat(foundToken).isEmpty();
        }
    }

    @Test
    void 토큰을_삭제한다() {
        // given
        redisTokenStorage.saveToken(subject, expectedToken);

        // when
        redisTokenStorage.deleteToken(subject, tokenClass);

        // then
        Optional<String> foundToken = redisTokenStorage.findToken(subject, tokenClass);
        assertThat(foundToken).isEmpty();
    }
}
