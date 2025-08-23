package com.example.solidconnection.auth.controller;

import com.example.solidconnection.auth.dto.EmailSignInRequest;
import com.example.solidconnection.auth.dto.EmailSignUpTokenRequest;
import com.example.solidconnection.auth.dto.EmailSignUpTokenResponse;
import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.dto.oauth.OAuthCodeRequest;
import com.example.solidconnection.auth.dto.oauth.OAuthResponse;
import com.example.solidconnection.auth.dto.oauth.OAuthSignInResponse;
import com.example.solidconnection.auth.service.AuthService;
import com.example.solidconnection.auth.service.EmailSignInService;
import com.example.solidconnection.auth.service.signup.EmailSignUpTokenProvider;
import com.example.solidconnection.auth.service.signup.SignUpService;
import com.example.solidconnection.auth.service.oauth.OAuthService;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.siteuser.domain.AuthType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final OAuthService oAuthService;
    private final SignUpService signUpService;
    private final EmailSignInService emailSignInService;
    private final EmailSignUpTokenProvider emailSignUpTokenProvider;
    private final RefreshTokenCookieManager refreshTokenCookieManager;

    @PostMapping("/apple")
    public ResponseEntity<OAuthResponse> processAppleOAuth(
            @Valid @RequestBody OAuthCodeRequest oAuthCodeRequest,
            HttpServletResponse httpServletResponse
    ) {
        OAuthResponse oAuthResponse = oAuthService.processOAuth(AuthType.APPLE, oAuthCodeRequest);
        if (oAuthResponse instanceof OAuthSignInResponse signInResponse) {
            refreshTokenCookieManager.setCookie(httpServletResponse, signInResponse.refreshToken());
        }
        return ResponseEntity.ok(oAuthResponse);
    }

    @PostMapping("/kakao")
    public ResponseEntity<OAuthResponse> processKakaoOAuth(
            @Valid @RequestBody OAuthCodeRequest oAuthCodeRequest,
            HttpServletResponse httpServletResponse
    ) {
        OAuthResponse oAuthResponse = oAuthService.processOAuth(AuthType.KAKAO, oAuthCodeRequest);
        if (oAuthResponse instanceof OAuthSignInResponse signInResponse) {
            refreshTokenCookieManager.setCookie(httpServletResponse, signInResponse.refreshToken());
        }
        return ResponseEntity.ok(oAuthResponse);
    }

    @PostMapping("/email/sign-in")
    public ResponseEntity<SignInResponse> signInWithEmail(
            @Valid @RequestBody EmailSignInRequest signInRequest,
            HttpServletResponse httpServletResponse
    ) {
        SignInResponse signInResponse = emailSignInService.signIn(signInRequest);
        refreshTokenCookieManager.setCookie(httpServletResponse, signInResponse.refreshToken());
        return ResponseEntity.ok(signInResponse);
    }

    /* 이메일 회원가입 시 signUpToken 을 발급받기 위한 api */
    @PostMapping("/email/sign-up")
    public ResponseEntity<EmailSignUpTokenResponse> signUpWithEmail(
            @Valid @RequestBody EmailSignUpTokenRequest signUpRequest
    ) {
        String signUpToken = emailSignUpTokenProvider.issueEmailSignUpToken(signUpRequest);
        return ResponseEntity.ok(new EmailSignUpTokenResponse(signUpToken));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignInResponse> signUp(
            @Valid @RequestBody SignUpRequest signUpRequest
    ) {
        SignInResponse signInResponse = signUpService.signUp(signUpRequest);
        return ResponseEntity.ok(signInResponse);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(
            Authentication authentication,
            HttpServletResponse httpServletResponse
    ) {
        String accessToken = getAccessToken(authentication);
        authService.signOut(accessToken);
        refreshTokenCookieManager.deleteCookie(httpServletResponse);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/quit")
    public ResponseEntity<Void> quit(
            @AuthorizedUser long siteUserId,
            Authentication authentication,
            HttpServletResponse httpServletResponse
    ) {
        String accessToken = getAccessToken(authentication);
        authService.quit(siteUserId, accessToken);
        refreshTokenCookieManager.deleteCookie(httpServletResponse);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissueToken(
            HttpServletRequest request
    ) {
        String refreshToken = refreshTokenCookieManager.getRefreshToken(request);
        ReissueResponse reissueResponse = authService.reissue(refreshToken);
        return ResponseEntity.ok(reissueResponse);
    }

    private String getAccessToken(Authentication authentication) {
        if (authentication == null || !(authentication.getCredentials() instanceof String accessToken)) {
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED, "엑세스 토큰이 없습니다.");
        }
        return accessToken;
    }
}
