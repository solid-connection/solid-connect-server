package com.example.solidconnection.auth.service;

import static com.example.solidconnection.auth.domain.TokenType.BLACKLIST;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.token.TokenBlackListService;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@DisplayName("토큰 블랙리스트 서비스 테스트")
@TestContainerSpringBootTest
class TokenBlackListServiceTest {

    @Autowired
    private TokenBlackListService tokenBlackListService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private AccessToken accessToken;

    @BeforeEach
    void setUp() {
        accessToken = new AccessToken("subject", Role.MENTEE, "token");
    }


    @Test
    void 액세스_토큰을_블랙리스트에_추가한다() {
        // when
        tokenBlackListService.addToBlacklist(accessToken);

        // then
        String blackListTokenKey = BLACKLIST.addPrefix(accessToken.token());
        String foundBlackListToken = redisTemplate.opsForValue().get(blackListTokenKey);
        assertThat(foundBlackListToken).isNotNull();
    }

    @Nested
    class 블랙리스트에_있는_토큰인지_확인한다 {

        @Test
        void 블랙리스트에_토큰이_있는_경우() {
            // given
            tokenBlackListService.addToBlacklist(accessToken);

            // when, then
            assertThat(tokenBlackListService.isTokenBlacklisted(accessToken.token())).isTrue();
        }

        @Test
        void 블랙리스트에_토큰이_없는_경우() {
            // when, then
            assertThat(tokenBlackListService.isTokenBlacklisted(accessToken.token())).isFalse();
        }
    }
}
