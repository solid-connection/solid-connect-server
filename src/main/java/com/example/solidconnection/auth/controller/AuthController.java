package com.example.solidconnection.auth.controller;

import com.example.solidconnection.auth.dto.EmailSignInRequest;
import com.example.solidconnection.auth.dto.EmailSignUpTokenRequest;
import com.example.solidconnection.auth.dto.EmailSignUpTokenResponse;
import com.example.solidconnection.auth.dto.ReissueRequest;
import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.dto.oauth.OAuthCodeRequest;
import com.example.solidconnection.auth.dto.oauth.OAuthResponse;
import com.example.solidconnection.auth.service.AuthService;
import com.example.solidconnection.auth.service.CommonSignUpTokenProvider;
import com.example.solidconnection.auth.service.EmailSignInService;
import com.example.solidconnection.auth.service.EmailSignUpService;
import com.example.solidconnection.auth.service.EmailSignUpTokenProvider;
import com.example.solidconnection.auth.service.oauth.AppleOAuthService;
import com.example.solidconnection.auth.service.oauth.KakaoOAuthService;
import com.example.solidconnection.auth.service.oauth.OAuthSignUpService;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.custom.resolver.AuthorizedUser;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
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
    private final OAuthSignUpService oAuthSignUpService;
    private final AppleOAuthService appleOAuthService;
    private final KakaoOAuthService kakaoOAuthService;
    private final EmailSignInService emailSignInService;
    private final EmailSignUpService emailSignUpService;
    private final EmailSignUpTokenProvider emailSignUpTokenProvider;
    private final CommonSignUpTokenProvider commonSignUpTokenProvider;

    @PostMapping("/apple")
    public ResponseEntity<OAuthResponse> processAppleOAuth(
            @Valid @RequestBody OAuthCodeRequest oAuthCodeRequest
    ) {
        OAuthResponse oAuthResponse = appleOAuthService.processOAuth(oAuthCodeRequest);
        return ResponseEntity.ok(oAuthResponse);
    }

    @PostMapping("/kakao")
    public ResponseEntity<OAuthResponse> processKakaoOAuth(
            @Valid @RequestBody OAuthCodeRequest oAuthCodeRequest
    ) {
        OAuthResponse oAuthResponse = kakaoOAuthService.processOAuth(oAuthCodeRequest);
        return ResponseEntity.ok(oAuthResponse);
    }

    @PostMapping("/email/sign-in")
    public ResponseEntity<SignInResponse> signInWithEmail(
            @Valid @RequestBody EmailSignInRequest signInRequest
    ) {
        SignInResponse signInResponse = emailSignInService.signIn(signInRequest);
        return ResponseEntity.ok(signInResponse);
    }

    /* 이메일 회원가입 시 signUpToken 을 발급받기 위한 api */
    @PostMapping("/email/sign-up")
    public ResponseEntity<EmailSignUpTokenResponse> signUpWithEmail(
            @Valid @RequestBody EmailSignUpTokenRequest signUpRequest
    ) {
        emailSignUpService.validateUniqueEmail(signUpRequest.email());
        String signUpToken = emailSignUpTokenProvider.generateAndSaveSignUpToken(signUpRequest);
        return ResponseEntity.ok(new EmailSignUpTokenResponse(signUpToken));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignInResponse> signUp(
            @Valid @RequestBody SignUpRequest signUpRequest
    ) {
        AuthType authType = commonSignUpTokenProvider.parseAuthType(signUpRequest.signUpToken());
        if (AuthType.isEmail(authType)) {
            SignInResponse signInResponse = emailSignUpService.signUp(signUpRequest);
            return ResponseEntity.ok(signInResponse);
        }
        SignInResponse signInResponse = oAuthSignUpService.signUp(signUpRequest);
        return ResponseEntity.ok(signInResponse);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(
            Authentication authentication
    ) {
        String accessToken = getAccessToken(authentication);
        authService.signOut(accessToken);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/quit")
    public ResponseEntity<Void> quit(
            @AuthorizedUser SiteUser siteUser,
            Authentication authentication // todo: #299를 작업하며 인자를 (Authentication authentication)만 받도록 수정해야 함
    ) {
        String accessToken = getAccessToken(authentication);
        authService.quit(siteUser, accessToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissueToken(
            @Valid @RequestBody ReissueRequest reissueRequest
    ) {
        ReissueResponse reissueResponse = authService.reissue(reissueRequest);
        return ResponseEntity.ok(reissueResponse);
    }

    private String getAccessToken(Authentication authentication) {
        if (authentication == null || !(authentication.getCredentials() instanceof String accessToken)) {
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED, "엑세스 토큰이 없습니다.");
        }
        return accessToken;
    }
}
