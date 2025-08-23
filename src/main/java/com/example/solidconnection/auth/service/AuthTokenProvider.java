package com.example.solidconnection.auth.service;

import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.domain.RefreshToken;
import com.example.solidconnection.auth.domain.Subject;
import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.token.config.TokenProperties;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthTokenProvider {

    private static final String ROLE_CLAIM_KEY = "role";

    private final TokenProvider tokenProvider;
    private final TokenStorage tokenStorage;
    private final SiteUserRepository siteUserRepository;
    private final TokenProperties tokenProperties;

    public AccessToken generateAccessToken(SiteUser siteUser) {
        Subject subject = toSubject(siteUser);
        Role role = siteUser.getRole();
        String token = tokenProvider.generateToken(
                subject,
                Map.of(ROLE_CLAIM_KEY, role.name()),
                tokenProperties.access().expireTime()
        );
        return new AccessToken(token);
    }

    public RefreshToken generateAndSaveRefreshToken(SiteUser siteUser) {
        Subject subject = toSubject(siteUser);
        String token = tokenProvider.generateToken(
                subject,
                tokenProperties.refresh().expireTime()
        );
        tokenStorage.saveToken(token, TokenType.REFRESH);
        return new RefreshToken(token);
    }

    /*
     * 유효한 리프레시 토큰인지 확인한다.
     * - 요청된 토큰과 같은 subject 의 리프레시 토큰을 조회한다.
     * - 조회된 리프레시 토큰과 요청된 토큰이 같은지 비교한다.
     * */
    public boolean isValidRefreshToken(String requestedRefreshToken) {
        Subject subject = tokenProvider.parseSubject(requestedRefreshToken);
        return tokenStorage.findToken(subject, TokenType.REFRESH)
                .map(foundRefreshToken -> Objects.equals(foundRefreshToken, requestedRefreshToken))
                .orElse(false);
    }

    public void deleteRefreshTokenByAccessToken(AccessToken accessToken) {
        Subject subject = tokenProvider.parseSubject(accessToken.token());
        tokenStorage.deleteToken(subject, TokenType.REFRESH);
    }

    public SiteUser parseSiteUser(String token) {
        Subject subject = tokenProvider.parseSubject(token);
        long siteUserId = Long.parseLong(subject.value());
        return siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    public Subject toSubject(SiteUser siteUser) {
        return new Subject(siteUser.getId().toString());
    }
}
