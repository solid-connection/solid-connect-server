package com.example.solidconnection.auth.controller;

import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.dto.oauth.OAuthCodeRequest;
import com.example.solidconnection.auth.dto.oauth.OAuthResponse;
import com.example.solidconnection.auth.service.AuthService;
import com.example.solidconnection.auth.service.oauth.AppleOAuthService;
import com.example.solidconnection.auth.service.oauth.KakaoOAuthService;
import com.example.solidconnection.auth.service.oauth.OAuthSignUpService;
import com.example.solidconnection.custom.resolver.AuthorizedUser;
import com.example.solidconnection.custom.resolver.ExpiredToken;
import com.example.solidconnection.custom.security.authentication.ExpiredTokenAuthentication;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @PostMapping("/sign-up")
    public ResponseEntity<SignInResponse> signUp(
            @Valid @RequestBody SignUpRequest signUpRequest
    ) {
        SignInResponse signInResponse = oAuthSignUpService.signUp(signUpRequest);
        return ResponseEntity.ok(signInResponse);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(
            @ExpiredToken ExpiredTokenAuthentication expiredToken
    ) {
        authService.signOut(expiredToken.getToken());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/quit")
    public ResponseEntity<Void> quit(
            @AuthorizedUser SiteUser siteUser
    ) {
        authService.quit(siteUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissueToken(
            @ExpiredToken ExpiredTokenAuthentication expiredToken
    ) {
        ReissueResponse reissueResponse = authService.reissue(expiredToken.getSubject());
        return ResponseEntity.ok(reissueResponse);
    }
}
