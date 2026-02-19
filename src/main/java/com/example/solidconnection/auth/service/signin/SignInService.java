package com.example.solidconnection.auth.service.signin;

import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.domain.RefreshToken;
import com.example.solidconnection.auth.dto.SignInResult;
import com.example.solidconnection.auth.service.AuthTokenProvider;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignInService {

    private final AuthTokenProvider authTokenProvider;

    @Transactional
    public SignInResult signIn(SiteUser siteUser) {
        resetQuitedAt(siteUser);
        AccessToken accessToken = authTokenProvider.generateAccessToken(siteUser);
        RefreshToken refreshToken = authTokenProvider.generateAndSaveRefreshToken(siteUser);
        return SignInResult.of(accessToken, refreshToken);
    }

    private void resetQuitedAt(SiteUser siteUser) {
        if (siteUser.getQuitedAt() == null) {
            return;
        }
        siteUser.setQuitedAt(null);
    }
}
