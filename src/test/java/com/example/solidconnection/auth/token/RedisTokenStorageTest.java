package com.example.solidconnection.auth.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.service.TokenProvider;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.util.Optional;
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

    private final String subject = "subject123";
    private final TokenType tokenType = TokenType.ACCESS;

    @Test
    void 토큰을_저장한다() {
        // given
        String expectedToken = tokenProvider.generateToken(subject, tokenType);

        // when
        String savedToken = redisTokenStorage.saveToken(expectedToken, tokenType);

        // then
        Optional<String> foundToken = redisTokenStorage.findToken(subject, tokenType);
        assertAll(
                () -> assertThat(foundToken).hasValue(expectedToken),
                () -> assertThat(savedToken).isEqualTo(expectedToken)
        );
    }

    @Nested
    class 토큰을_조회한다 {

        @Test
        void 저장된_토큰이_있으면_Optional에_담아_반한다() {
            // given
            String expectedToken = tokenProvider.generateToken(subject, tokenType);
            redisTokenStorage.saveToken(expectedToken, tokenType);

            // when
            Optional<String> foundToken = redisTokenStorage.findToken(subject, tokenType);

            // then
            assertThat(foundToken).hasValue(expectedToken);
        }

        @Test
        void 저장된_토큰이_없으면_빈_Optional을_반환한다() {
            // when
            Optional<String> foundToken = redisTokenStorage.findToken(subject, tokenType);

            // then
            assertThat(foundToken).isEmpty();
        }
    }

    @Test
    void 토큰을_삭제한다() {
        // given
        String expectedToken = tokenProvider.generateToken(subject, tokenType);
        redisTokenStorage.saveToken(expectedToken, tokenType);

        // when
        redisTokenStorage.deleteToken(subject, tokenType);

        // then
        Optional<String> foundToken = redisTokenStorage.findToken(subject, tokenType);
        assertThat(foundToken).isEmpty();
    }
}
