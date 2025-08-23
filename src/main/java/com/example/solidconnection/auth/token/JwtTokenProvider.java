package com.example.solidconnection.auth.token;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_TOKEN;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.service.TokenProvider;
import com.example.solidconnection.auth.token.config.JwtProperties;
import com.example.solidconnection.common.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenProvider {

    private final JwtProperties jwtProperties;

    @Override
    public final String generateToken(String string, TokenType tokenType) {
        return generateJwtTokenValue(string, Map.of(), tokenType.getExpireTime());
    }

    @Override
    public String generateToken(String string, Map<String, String> customClaims, TokenType tokenType) {
        return generateJwtTokenValue(string, customClaims, tokenType.getExpireTime());
    }

    private String generateJwtTokenValue(String subject, Map<String, String> claims, long expireTime) {
        Claims jwtClaims = Jwts.claims().setSubject(subject);
        jwtClaims.putAll(claims);
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + expireTime);
        return Jwts.builder()
                .setClaims(jwtClaims)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.secret())
                .compact();
    }

    @Override
    public final String saveToken(String token, TokenType tokenType) {
        String subject = parseSubject(token);
        redisTemplate.opsForValue().set(
                tokenType.addPrefix(subject),
                token,
                tokenType.getExpireTime(),
                TimeUnit.MILLISECONDS
        );
        return token;
    }

    @Override
    public String parseSubject(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtProperties.secret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new CustomException(INVALID_TOKEN);
        }
    }
}
