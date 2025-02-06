package com.example.solidconnection.auth.service.oauth;

import com.example.solidconnection.auth.client.KakaoOAuthClient;
import com.example.solidconnection.auth.dto.oauth.OAuthUserInfoDto;
import com.example.solidconnection.auth.service.SignInService;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import org.springframework.stereotype.Service;

@Service
public class KakaoOAuthService extends OAuthService {

    private final KakaoOAuthClient kakaoOAuthClient;

    public KakaoOAuthService(SignUpTokenProvider signUpTokenProvider, SiteUserRepository siteUserRepository,
                             KakaoOAuthClient kakaoOAuthClient, SignInService signInService) {
        super(signUpTokenProvider, siteUserRepository, signInService);
        this.kakaoOAuthClient = kakaoOAuthClient;
    }

    @Override
    protected OAuthUserInfoDto getOAuthUserInfo(String code) {
        return kakaoOAuthClient.getUserInfo(code);
    }

    @Override
    protected AuthType getAuthType() {
        return AuthType.KAKAO;
    }
}
