package com.example.solidconnection.auth.service.signup;

import static com.example.solidconnection.common.exception.ErrorCode.SIGN_UP_TOKEN_INVALID;
import static com.example.solidconnection.common.exception.ErrorCode.SIGN_UP_TOKEN_NOT_ISSUED_BY_SERVER;

import com.example.solidconnection.auth.domain.SignUpToken;
import com.example.solidconnection.auth.domain.Subject;
import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.service.TokenProvider;
import com.example.solidconnection.auth.service.TokenStorage;
import com.example.solidconnection.auth.token.config.TokenProperties;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.AuthType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignUpTokenProvider {

    private static final String AUTH_TYPE_CLAIM_KEY = "authType";

    private final TokenProvider tokenProvider;
    private final TokenStorage tokenStorage;
    private final TokenProperties tokenProperties;

    public SignUpToken generateAndSaveSignUpToken(String email, AuthType authType) {
        String token = tokenProvider.generateToken(
                new Subject(email),
                Map.of(AUTH_TYPE_CLAIM_KEY, authType.toString()),
                tokenProperties.signUp().expireTime()
        );
        tokenStorage.saveToken(token, TokenType.SIGN_UP);
        return new SignUpToken(token);
    }

    public void deleteByEmail(String email) {
        tokenStorage.deleteToken(email, TokenType.SIGN_UP);
    }

    public void validateSignUpToken(String token) {
        validateFormatAndExpiration(token);
        String email = parseEmail(token);
        validateIssuedByServer(email);
    }

    private void validateFormatAndExpiration(String token) { // 파싱되는지, AuthType이 포함되어있는지 검증
        try {
            String serializedAuthType = tokenProvider.parseClaims(token, AUTH_TYPE_CLAIM_KEY, String.class);
            AuthType.valueOf(serializedAuthType);
        } catch (Exception e) {
            throw new CustomException(SIGN_UP_TOKEN_INVALID);
        }
    }

    private void validateIssuedByServer(String email) {
        tokenStorage.findToken(email, TokenType.SIGN_UP)
                .orElseThrow(() -> new CustomException(SIGN_UP_TOKEN_NOT_ISSUED_BY_SERVER));
    }

    public String parseEmail(String token) {
        return tokenProvider.parseSubject(token);
    }

    public AuthType parseAuthType(String token) {
        String serializedAuthType = tokenProvider.parseClaims(token, AUTH_TYPE_CLAIM_KEY, String.class);
        return AuthType.valueOf(serializedAuthType);
    }
}
