package com.example.solidconnection.auth.token;

import com.example.solidconnection.auth.domain.Subject;
import com.example.solidconnection.auth.domain.Token;
import com.example.solidconnection.auth.service.TokenStorage;
import com.example.solidconnection.auth.token.config.TokenProperties;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisTokenStorage implements TokenStorage {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public <T extends Token> T saveToken(Subject subject, T token) {
        redisTemplate.opsForValue().set(
                createKey(subject, token.getClass()),
                token.token(),
                TokenProperties.getExpireTime(token.getClass())
        );
        return token;
    }

    @Override
    public <T extends Token> Optional<String> findToken(Subject subject, Class<T> tokenClass) {
        String key = createKey(subject, tokenClass);
        String foundTokenValue = redisTemplate.opsForValue().get(key);
        if (foundTokenValue == null || foundTokenValue.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(foundTokenValue);
    }

    @Override
    public <T extends Token> void deleteToken(Subject subject, Class<T> tokenClass) {
        String key = createKey(subject, tokenClass);
        redisTemplate.delete(key);
    }

    private <T extends Token> String createKey(Subject subject, Class<T> tokenClass) {
        return TokenProperties.getStorageKeyPrefix(tokenClass) + ":" + subject.value();
    }
}
