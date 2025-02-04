package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.siteuser.domain.SiteUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.example.solidconnection.util.JwtUtils.parseSubject;
import static com.example.solidconnection.util.JwtUtils.parseSubjectIgnoringExpiration;

@RequiredArgsConstructor
@Component
public class TokenProvider {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    public String generateToken(SiteUser siteUser, TokenType tokenType) {
        String subject = siteUser.getId().toString();
        return generateToken(subject, tokenType);
    }

    public String generateToken(String string, TokenType tokenType) {
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

    public String saveToken(String token, TokenType tokenType) {
        String subject = parseSubject(token, jwtProperties.secret());
        redisTemplate.opsForValue().set(
                tokenType.addPrefixToSubject(subject),
                token,
                tokenType.getExpireTime(),
                TimeUnit.MILLISECONDS
        );
        return token;
    }

    public String getEmail(String token) {
        return parseSubjectIgnoringExpiration(token, jwtProperties.secret());
    }
}
