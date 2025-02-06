package com.example.solidconnection.auth.service.oauth;

import com.example.solidconnection.auth.client.AppleOAuthClient;
import com.example.solidconnection.auth.dto.oauth.OAuthUserInfoDto;
import com.example.solidconnection.auth.service.SignInService;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import org.springframework.stereotype.Service;

@Service
public class AppleOAuthService extends OAuthService {

    private final AppleOAuthClient appleOAuthClient;

    public AppleOAuthService(SignUpTokenProvider signUpTokenProvider, SiteUserRepository siteUserRepository,
                             AppleOAuthClient appleOAuthClient, SignInService signInService) {
        super(signUpTokenProvider, siteUserRepository, signInService);
        this.appleOAuthClient = appleOAuthClient;
    }

    @Override
    protected OAuthUserInfoDto getOAuthUserInfo(String code) {
        return appleOAuthClient.processOAuth(code);
    }

    @Override
    protected AuthType getAuthType() {
        return AuthType.APPLE;
    }
}
