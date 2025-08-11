package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.dto.EmailSignUpTokenRequest;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSignUpTokenProvider {

    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final SignUpTokenProvider signUpTokenProvider;
    private final SiteUserRepository siteUserRepository;

    public String issueEmailSignUpToken(EmailSignUpTokenRequest request) {
        String email = request.email();
        if (siteUserRepository.existsByEmailAndAuthType(email, AuthType.EMAIL)) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTED);
        }

        String signUpToken = signUpTokenProvider.generateAndSaveSignUpToken(email, AuthType.EMAIL);
        String password = request.password();
        String encodedPassword = passwordEncoder.encode(password);
        // todo: 비밀번호 임시 저장 로직 추가

        return tokenProvider.saveToken(signUpToken, TokenType.SIGN_UP);
    }

    public String getTemporarySavedPassword(String signUpToken) {
        // todo: 임시 저장된 비밀번호를 가져오는 로직 추가
        return "";
    }
}
