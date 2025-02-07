package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.dto.EmailSignUpTokenRequest;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.AuthType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.example.solidconnection.custom.exception.ErrorCode.SIGN_UP_TOKEN_INVALID;
import static com.example.solidconnection.custom.exception.ErrorCode.SIGN_UP_TOKEN_NOT_ISSUED_BY_SERVER;
import static com.example.solidconnection.util.JwtUtils.parseClaims;
import static com.example.solidconnection.util.JwtUtils.parseSubject;

@Component
public class EmailSignUpTokenProvider extends TokenProvider {

    static final String PASSWORD_CLAIM_KEY = "password";
    static final String AUTH_TYPE_CLAIM_KEY = "authType";

    private final PasswordEncoder passwordEncoder;

    public EmailSignUpTokenProvider(JwtProperties jwtProperties, RedisTemplate<String, String> redisTemplate,
                                    PasswordEncoder passwordEncoder) {
        super(jwtProperties, redisTemplate);
        this.passwordEncoder = passwordEncoder;
    }

    public String generateAndSaveSignUpToken(EmailSignUpTokenRequest request) {
        String email = request.email();
        String password = request.password();
        String encodedPassword = passwordEncoder.encode(password);
        Map<String, Object> emailSignUpClaims = new HashMap<>(Map.of(
                PASSWORD_CLAIM_KEY, encodedPassword,
                AUTH_TYPE_CLAIM_KEY, AuthType.EMAIL
        ));
        Claims claims = Jwts.claims(emailSignUpClaims).setSubject(email);
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + TokenType.SIGN_UP.getExpireTime());

        String signUpToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.secret())
                .compact();
        return saveToken(signUpToken, TokenType.SIGN_UP);
    }

    public void validateSignUpToken(String token) {
        validateFormatAndExpiration(token);
        String email = parseEmail(token);
        validateIssuedByServer(email);
    }

    private void validateFormatAndExpiration(String token) {
        try {
            Claims claims = parseClaims(token, jwtProperties.secret());
            Objects.requireNonNull(claims.getSubject());
            String encodedPassword = claims.get(PASSWORD_CLAIM_KEY, String.class);
            Objects.requireNonNull(encodedPassword);
        } catch (Exception e) {
            throw new CustomException(SIGN_UP_TOKEN_INVALID);
        }
    }

    private void validateIssuedByServer(String email) {
        String key = TokenType.SIGN_UP.addPrefix(email);
        if (redisTemplate.opsForValue().get(key) == null) {
            throw new CustomException(SIGN_UP_TOKEN_NOT_ISSUED_BY_SERVER);
        }
    }

    public String parseEmail(String token) {
        return parseSubject(token, jwtProperties.secret());
    }

    public String parseEncodedPassword(String token) {
        Claims claims = parseClaims(token, jwtProperties.secret());
        return claims.get(PASSWORD_CLAIM_KEY, String.class);
    }
}
