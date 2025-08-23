package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.domain.RefreshToken;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignInService {

    private final AuthTokenProvider authTokenProvider;

    @Transactional
    public SignInResponse signIn(SiteUser siteUser) {
        resetQuitedAt(siteUser);
        AccessToken accessToken = authTokenProvider.generateAccessToken(siteUser);
        RefreshToken refreshToken = authTokenProvider.generateAndSaveRefreshToken(siteUser);
        return SignInResponse.of(accessToken, refreshToken);
    }

    private void resetQuitedAt(SiteUser siteUser) {
        if (siteUser.getQuitedAt() == null) {
            return;
        }
        siteUser.setQuitedAt(null);
    }
}
