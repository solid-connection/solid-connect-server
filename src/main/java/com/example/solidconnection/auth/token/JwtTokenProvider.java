package com.example.solidconnection.auth.token;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_TOKEN;

import com.example.solidconnection.auth.domain.Subject;
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
    public String generateToken(Subject subject, long expireTime) {
        return generateJwtTokenValue(subject.value(), Map.of(), expireTime);
    }

    @Override
    public String generateToken(Subject subject, Map<String, String> customClaims, long expireTime) {
        return generateJwtTokenValue(subject.value(), customClaims, expireTime);
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
    public Subject parseSubject(String token) {
        return new Subject(parseJwtClaims(token).getSubject());
    }

    @Override
    public <T> T parseClaims(String token, String claimName, Class<T> claimType) {
        return parseJwtClaims(token).get(claimName, claimType);
    }

    private Claims parseJwtClaims(String token) {
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
