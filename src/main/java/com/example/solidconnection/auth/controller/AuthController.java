package com.example.solidconnection.auth.controller;

import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.dto.SignUpResponse;
import com.example.solidconnection.auth.dto.kakao.KakaoCodeRequest;
import com.example.solidconnection.auth.dto.kakao.KakaoOauthResponse;
import com.example.solidconnection.auth.service.AuthService;
import com.example.solidconnection.auth.service.SignInService;
import com.example.solidconnection.auth.service.SignUpService;
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
    private final SignUpService signUpService;
    private final SignInService signInService;

    @PostMapping("/kakao")
    public ResponseEntity<KakaoOauthResponse> processKakaoOauth(
            @RequestBody KakaoCodeRequest kakaoCodeRequest
    ) {
        KakaoOauthResponse kakaoOauthResponse = signInService.signIn(kakaoCodeRequest);
        return ResponseEntity.ok(kakaoOauthResponse);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> signUp(
            @Valid @RequestBody SignUpRequest signUpRequest
    ) {
        SignUpResponse signUpResponseDto = signUpService.signUp(signUpRequest);
        return ResponseEntity.ok(signUpResponseDto);
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
