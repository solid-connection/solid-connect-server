package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.EmailSignInRequest;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.USER_NOT_FOUND;

/*
 * 보안을 위해 이메일과 비밀번호 중 무엇이 틀렸는지 구체적으로 응답하지 않는다.
 * */
@Service
@RequiredArgsConstructor
public class EmailSignInService {

    private final SignInService signInService;
    private final SiteUserRepository siteUserRepository;
    private final PasswordEncoder passwordEncoder;

    public SignInResponse signIn(EmailSignInRequest signInRequest) {
        Optional<SiteUser> optionalSiteUser = siteUserRepository.findByEmailAndAuthType(signInRequest.email(), AuthType.EMAIL);
        if (optionalSiteUser.isPresent()) {
            SiteUser siteUser = optionalSiteUser.get();
            validatePassword(signInRequest.password(), siteUser.getPassword());
            return signInService.signIn(siteUser);
        }
        throw new CustomException(USER_NOT_FOUND, "이메일과 비밀번호를 확인해주세요.");
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new CustomException(USER_NOT_FOUND, "이메일과 비밀번호를 확인해주세요.");
        }
    }
}
