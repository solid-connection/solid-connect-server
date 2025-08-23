package com.example.solidconnection.auth.token;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.service.TokenProvider;
import com.example.solidconnection.auth.service.TokenStorage;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisTokenStorage implements TokenStorage {

    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String saveToken(String token, TokenType tokenType) {
        String subject = tokenProvider.parseSubject(token);
        redisTemplate.opsForValue().set(
                createKey(subject, tokenType),
                token,
                tokenType.getExpireTime(),
                TimeUnit.MILLISECONDS
        );
        return token;
    }

    @Override
    public Optional<String> findToken(String subject, TokenType tokenType) {
        String key = createKey(subject, tokenType);
        String foundTokenValue = redisTemplate.opsForValue().get(key);
        if (foundTokenValue == null || foundTokenValue.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(foundTokenValue);
    }

    @Override
    public void deleteToken(String subject, TokenType tokenType) {
        String key = createKey(subject, tokenType);
        redisTemplate.delete(key);
    }

    private String createKey(String subject, TokenType tokenType) {
        return tokenType.addPrefix(subject);
    }
}
