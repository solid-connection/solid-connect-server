package com.example.solidconnection.auth.service.oauth;


import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.oauth.OAuthCodeRequest;
import com.example.solidconnection.auth.dto.oauth.OAuthResponse;
import com.example.solidconnection.auth.dto.oauth.OAuthSignInResponse;
import com.example.solidconnection.auth.dto.oauth.OAuthUserInfoDto;
import com.example.solidconnection.auth.dto.oauth.SignUpPrepareResponse;
import com.example.solidconnection.auth.service.SignInService;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/*
 * OAuth 제공자로부터 이메일을 받아 기존 회원인지, 신규 회원인지 판별하고, 이에 따라 다르게 응답한다.
 * 기존 회원 : 로그인한다.
 * 신규 회원 : 회원가입할 때 필요한 정보를 제공한다.
 * */
public abstract class OAuthService {

    private final OAuthSignUpTokenProvider OAuthSignUpTokenProvider;
    private final SignInService signInService;
    private final SiteUserRepository siteUserRepository;

    protected OAuthService(OAuthSignUpTokenProvider OAuthSignUpTokenProvider, SiteUserRepository siteUserRepository, SignInService signInService) {
        this.OAuthSignUpTokenProvider = OAuthSignUpTokenProvider;
        this.siteUserRepository = siteUserRepository;
        this.signInService = signInService;
    }

    @Transactional
    public OAuthResponse processOAuth(OAuthCodeRequest oauthCodeRequest) {
        OAuthUserInfoDto userInfoDto = getOAuthUserInfo(oauthCodeRequest.code());
        String email = userInfoDto.getEmail();
        Optional<SiteUser> optionalSiteUser = siteUserRepository.findByEmailAndAuthType(email, getAuthType());

        if (optionalSiteUser.isPresent()) {
            SiteUser siteUser = optionalSiteUser.get();
            return getSignInResponse(siteUser);
        }

        return getSignUpPrepareResponse(userInfoDto);
    }

    protected final OAuthSignInResponse getSignInResponse(SiteUser siteUser) {
        SignInResponse signInResponse = signInService.signIn(siteUser);
        return new OAuthSignInResponse(true, signInResponse.accessToken(), signInResponse.refreshToken());
    }

    protected final SignUpPrepareResponse getSignUpPrepareResponse(OAuthUserInfoDto userInfoDto) {
        String signUpToken = OAuthSignUpTokenProvider.generateAndSaveSignUpToken(userInfoDto.getEmail(), getAuthType());
        return SignUpPrepareResponse.of(userInfoDto, signUpToken);
    }

    protected abstract OAuthUserInfoDto getOAuthUserInfo(String code);
    protected abstract AuthType getAuthType();
}
