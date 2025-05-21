package com.example.solidconnection.auth.token;

import com.example.solidconnection.auth.service.AccessToken;
import com.example.solidconnection.security.filter.BlacklistChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.auth.domain.TokenType.BLACKLIST;

@Component
@RequiredArgsConstructor
public class TokenBlackListService implements BlacklistChecker {

    private static final String SIGN_OUT_VALUE = "signOut";

    private final RedisTemplate<String, String> redisTemplate;

    /*
     * 액세스 토큰을 블랙리스트에 저장한다.
     * - key = BLACKLIST:{accessToken}
     * - value = {SIGN_OUT_VALUE} -> key 의 존재만 확인하므로, value 에는 무슨 값이 들어가도 상관없다.
     * */
    public void addToBlacklist(AccessToken accessToken) {
        String blackListKey = BLACKLIST.addPrefix(accessToken.token());
        redisTemplate.opsForValue().set(blackListKey, SIGN_OUT_VALUE);
    }

    @Override
    public boolean isTokenBlacklisted(String accessToken) {
        String blackListTokenKey = BLACKLIST.addPrefix(accessToken);
        return redisTemplate.hasKey(blackListTokenKey);
    }
}
