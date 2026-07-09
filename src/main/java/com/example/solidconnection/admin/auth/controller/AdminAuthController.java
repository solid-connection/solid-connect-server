package com.example.solidconnection.admin.auth.controller;

import com.example.solidconnection.admin.auth.dto.AdminReissueResponse;
import com.example.solidconnection.admin.auth.dto.AdminSignInRequest;
import com.example.solidconnection.admin.auth.dto.AdminSignInResponse;
import com.example.solidconnection.admin.auth.dto.AdminSignInResult;
import com.example.solidconnection.admin.auth.service.AdminAuthService;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final AdminRefreshTokenCookieManager adminRefreshTokenCookieManager;

    @PostMapping("/sign-in")
    public ResponseEntity<AdminSignInResponse> signIn(
            @RequestBody @Valid AdminSignInRequest request,
            HttpServletResponse response
    ) {
        AdminSignInResult result = adminAuthService.signIn(request);
        adminRefreshTokenCookieManager.setCookie(response, result.adminRefreshToken());
        return ResponseEntity.ok(AdminSignInResponse.from(result.accessToken()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<AdminReissueResponse> reissue(HttpServletRequest request) {
        String adminRefreshToken = adminRefreshTokenCookieManager.getAdminRefreshToken(request);
        AdminReissueResponse reissueResponse = adminAuthService.reissue(adminRefreshToken);
        return ResponseEntity.ok(reissueResponse);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(
            Authentication authentication,
            HttpServletResponse response
    ) {
        String accessToken = getAccessToken(authentication);
        adminAuthService.signOut(accessToken);
        adminRefreshTokenCookieManager.deleteCookie(response);
        return ResponseEntity.ok().build();
    }

    private String getAccessToken(Authentication authentication) {
        if (authentication == null || !(authentication.getCredentials() instanceof String accessToken)) {
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED, "엑세스 토큰이 없습니다.");
        }
        return accessToken;
    }
}
