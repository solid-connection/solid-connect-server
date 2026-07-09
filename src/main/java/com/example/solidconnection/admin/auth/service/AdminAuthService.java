package com.example.solidconnection.admin.auth.service;

import static com.example.solidconnection.common.exception.ErrorCode.ADMIN_REFRESH_TOKEN_EXPIRED;
import static com.example.solidconnection.common.exception.ErrorCode.NOT_ADMIN_USER;
import static com.example.solidconnection.common.exception.ErrorCode.SIGN_IN_FAILED;

import com.example.solidconnection.admin.auth.dto.AdminReissueResponse;
import com.example.solidconnection.admin.auth.dto.AdminSignInRequest;
import com.example.solidconnection.admin.auth.dto.AdminSignInResult;
import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.domain.AdminRefreshToken;
import com.example.solidconnection.auth.exception.AuthException;
import com.example.solidconnection.auth.service.AuthTokenProvider;
import com.example.solidconnection.auth.token.TokenBlackListService;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AuthTokenProvider authTokenProvider;
    private final TokenBlackListService tokenBlackListService;
    private final SiteUserRepository siteUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminSignInResult signIn(AdminSignInRequest request) {
        SiteUser siteUser = getEmailMatchingUserOrThrow(request.email());
        validatePassword(request.password(), siteUser.getPassword());
        validateAdminRole(siteUser);
        resetQuitedAt(siteUser);
        AccessToken accessToken = authTokenProvider.generateAccessToken(siteUser);
        AdminRefreshToken adminRefreshToken = authTokenProvider.generateAndSaveAdminRefreshToken(siteUser);
        return AdminSignInResult.of(accessToken, adminRefreshToken);
    }

    private SiteUser getEmailMatchingUserOrThrow(String email) {
        return siteUserRepository.findByEmailAndAuthType(email, AuthType.EMAIL)
                .orElseThrow(() -> new CustomException(SIGN_IN_FAILED));
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new CustomException(SIGN_IN_FAILED);
        }
    }

    private void validateAdminRole(SiteUser siteUser) {
        if (!Role.ADMIN.equals(siteUser.getRole())) {
            throw new CustomException(NOT_ADMIN_USER);
        }
    }

    private void resetQuitedAt(SiteUser siteUser) {
        if (siteUser.getQuitedAt() == null) {
            return;
        }
        siteUser.setQuitedAt(null);
    }

    public AdminReissueResponse reissue(String requestedAdminRefreshToken) {
        if (!authTokenProvider.isValidAdminRefreshToken(requestedAdminRefreshToken)) {
            throw new AuthException(ADMIN_REFRESH_TOKEN_EXPIRED);
        }
        SiteUser siteUser = authTokenProvider.parseSiteUser(requestedAdminRefreshToken);
        AccessToken newAccessToken = authTokenProvider.generateAccessToken(siteUser);
        return AdminReissueResponse.from(newAccessToken);
    }

    public void signOut(String accessToken) {
        tokenBlackListService.addToBlacklist(accessToken);
        authTokenProvider.deleteAdminRefreshTokenByAccessToken(accessToken);
    }
}
