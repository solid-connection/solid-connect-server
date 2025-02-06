package com.example.solidconnection.auth.service;

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
        String accessToken = authTokenProvider.generateAccessToken(siteUser);
        String refreshToken = authTokenProvider.generateAndSaveRefreshToken(siteUser);
        return new SignInResponse(accessToken, refreshToken);
    }

    private void resetQuitedAt(SiteUser siteUser) {
        if (siteUser.getQuitedAt() == null) {
            return;
        }
        siteUser.setQuitedAt(null);
    }
}
