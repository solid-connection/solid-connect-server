package com.example.solidconnection.auth.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.AuthType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.auth.service.EmailSignUpTokenProvider.AUTH_TYPE_CLAIM_KEY;
import static com.example.solidconnection.common.exception.ErrorCode.SIGN_UP_TOKEN_INVALID;

@Component
@RequiredArgsConstructor
public class CommonSignUpTokenProvider {

    private final TokenProvider tokenProvider;

    public AuthType parseAuthType(String signUpToken) {
        try {
            String authTypeStr = tokenProvider.parseClaims(signUpToken).get(AUTH_TYPE_CLAIM_KEY, String.class);
            return AuthType.valueOf(authTypeStr);
        } catch (Exception e) {
            throw new CustomException(SIGN_UP_TOKEN_INVALID);
        }
    }
}
