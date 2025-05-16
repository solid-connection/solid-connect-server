package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.example.solidconnection.util.JwtUtils.parseSubject;

public abstract class TokenProvider {

    protected final JwtProperties jwtProperties;
    protected final RedisTemplate<String, String> redisTemplate;

    public TokenProvider(JwtProperties jwtProperties, RedisTemplate<String, String> redisTemplate) {
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
    }

    protected final String generateToken(String string, TokenType tokenType) {
        Claims claims = Jwts.claims().setSubject(string);
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + tokenType.getExpireTime());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.secret())
                .compact();
    }

    protected final String saveToken(String token, TokenType tokenType) {
        String subject = parseSubject(token, jwtProperties.secret());
        redisTemplate.opsForValue().set(
                tokenType.addPrefix(subject),
                token,
                tokenType.getExpireTime(),
                TimeUnit.MILLISECONDS
        );
        return token;
    }
}
