package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordTemporaryStorage {

    private static final String KEY_PREFIX = "password:";

    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public void save(String email, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        redisTemplate.opsForValue().set(
                convertToKey(email),
                encodedPassword,
                TokenType.SIGN_UP.getExpireTime(),
                TimeUnit.MILLISECONDS
        );
    }

    public Optional<String> findByEmail(String email) {
        String encodedPassword = redisTemplate.opsForValue().get(convertToKey(email));
        if (encodedPassword == null) {
            return Optional.empty();
        }
        return Optional.of(encodedPassword);
    }

    public void deleteByEmail(String email) {
        String key = convertToKey(email);
        redisTemplate.delete(key);
    }

    private String convertToKey(String email) {
        return KEY_PREFIX + email;
    }
}
