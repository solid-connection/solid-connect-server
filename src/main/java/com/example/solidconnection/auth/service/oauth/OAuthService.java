package com.example.solidconnection.auth.service.oauth;

import com.example.solidconnection.auth.domain.SignUpToken;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.oauth.OAuthCodeRequest;
import com.example.solidconnection.auth.dto.oauth.OAuthResponse;
import com.example.solidconnection.auth.dto.oauth.OAuthSignInResponse;
import com.example.solidconnection.auth.dto.oauth.OAuthUserInfoDto;
import com.example.solidconnection.auth.dto.oauth.SignUpPrepareResponse;
import com.example.solidconnection.auth.service.signin.SignInService;
import com.example.solidconnection.auth.service.signup.SignUpTokenProvider;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * OAuth 제공자로부터 이메일을 받아 기존 회원인지, 신규 회원인지 판별하고, 이에 따라 다르게 응답한다.
 * 기존 회원 : 로그인한다.
 * 신규 회원 : 회원가입할 때 필요한 정보를 제공한다.
 * */
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final SignUpTokenProvider signUpTokenProvider;
    private final SignInService signInService;
    private final SiteUserRepository siteUserRepository;
    private final OAuthClientMap oauthClientMap;

    @Transactional
    public OAuthResponse processOAuth(AuthType authType, OAuthCodeRequest codeRequest) {
        OAuthClient oauthClient = oauthClientMap.getOAuthClient(authType);
        OAuthUserInfoDto userInfo = oauthClient.getUserInfo(codeRequest.code());
        Optional<SiteUser> optionalSiteUser = siteUserRepository.findByEmailAndAuthType(userInfo.getEmail(), authType);

        if (optionalSiteUser.isPresent()) {
            SiteUser siteUser = optionalSiteUser.get();
            return getSignInResponse(siteUser);
        }

        return getSignUpPrepareResponse(userInfo, authType);
    }

    private OAuthSignInResponse getSignInResponse(SiteUser siteUser) {
        SignInResponse signInResponse = signInService.signIn(siteUser);
        return new OAuthSignInResponse(signInResponse.accessToken(), signInResponse.refreshToken());
    }

    private SignUpPrepareResponse getSignUpPrepareResponse(OAuthUserInfoDto userInfoDto, AuthType authType) {
        SignUpToken signUpToken = signUpTokenProvider.generateAndSaveSignUpToken(userInfoDto.getEmail(), authType);
        return SignUpPrepareResponse.of(userInfoDto, signUpToken.token());
    }
}
