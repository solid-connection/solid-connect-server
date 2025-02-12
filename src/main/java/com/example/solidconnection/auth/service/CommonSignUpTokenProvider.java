package com.example.solidconnection.auth.service;

import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.auth.service.EmailSignUpTokenProvider.AUTH_TYPE_CLAIM_KEY;
import static com.example.solidconnection.custom.exception.ErrorCode.SIGN_UP_TOKEN_INVALID;

@Component
@RequiredArgsConstructor
public class CommonSignUpTokenProvider {

    private final JwtProperties jwtProperties;

    public AuthType parseAuthType(String signUpToken) {
        try {
            String authTypeStr = JwtUtils.parseClaims(signUpToken, jwtProperties.secret()).get(AUTH_TYPE_CLAIM_KEY, String.class);
            return AuthType.valueOf(authTypeStr);
        } catch (Exception e) {
            throw new CustomException(SIGN_UP_TOKEN_INVALID);
        }
    }
}
