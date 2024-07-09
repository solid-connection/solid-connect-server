package com.example.solidconnection.auth.controller;

import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.dto.SignUpResponse;
import com.example.solidconnection.auth.dto.kakao.KakaoCodeRequest;
import com.example.solidconnection.auth.dto.kakao.KakaoOauthResponse;
import com.example.solidconnection.auth.service.AuthService;
import com.example.solidconnection.auth.service.SignInService;
import com.example.solidconnection.auth.service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final SignUpService signUpService;
    private final SignInService signInService;

    //TODO: 추후 OAuth 추가를 염두에 둔다면 "/sign-in/kakao" 로 바꿔야 할 것 같다.
    @PostMapping("/kakao")
    public ResponseEntity<KakaoOauthResponse> processKakaoOauth(@RequestBody KakaoCodeRequest kakaoCodeRequest) {
        KakaoOauthResponse kakaoOauthResponse = signInService.signIn(kakaoCodeRequest);
        return ResponseEntity
                .ok(kakaoOauthResponse);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        SignUpResponse signUpResponseDto = signUpService.signUp(signUpRequest);
        return ResponseEntity
                .ok(signUpResponseDto);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(Principal principal) {
        authService.signOut(principal.getName());
        return ResponseEntity
                .noContent()
                .build();
    }

    @PatchMapping("/quit")
    public ResponseEntity<Void> quit(Principal principal) {
        authService.quit(principal.getName());
        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissueToken(Principal principal) {
        ReissueResponse reissueResponse = authService.reissue(principal.getName());
        return ResponseEntity
                .ok(reissueResponse);
    }
}
