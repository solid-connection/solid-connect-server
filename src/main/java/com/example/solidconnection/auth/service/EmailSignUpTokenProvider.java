package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.EmailSignUpTokenRequest;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class EmailSignUpTokenProvider {

    private final SignUpTokenProvider signUpTokenProvider;
    private final SiteUserRepository siteUserRepository;
    private final PasswordTemporaryStorage passwordTemporaryStorage;

    @Transactional(readOnly = true)
    public String issueEmailSignUpToken(EmailSignUpTokenRequest request) {
        String email = request.email();
        String password = request.password();

        if (siteUserRepository.existsByEmailAndAuthType(email, AuthType.EMAIL)) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTED);
        }

        passwordTemporaryStorage.save(email, password);
        return signUpTokenProvider.generateAndSaveSignUpToken(email, AuthType.EMAIL);
    }
}
