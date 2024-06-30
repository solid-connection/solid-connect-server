package com.example.solidconnection.auth.controller;

import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.dto.SignUpResponse;
import com.example.solidconnection.auth.dto.kakao.KakaoCodeRequest;
import com.example.solidconnection.auth.dto.kakao.KakaoOauthResponse;
import com.example.solidconnection.auth.service.AuthService;
import com.example.solidconnection.auth.service.SignInService;
import com.example.solidconnection.auth.service.SignUpService;
import com.example.solidconnection.custom.response.CustomResponse;
import com.example.solidconnection.custom.response.DataResponse;
import com.example.solidconnection.custom.response.StatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public CustomResponse processKakaoOauth(@RequestBody KakaoCodeRequest kakaoCodeRequest) {
        KakaoOauthResponse kakaoOauthResponse = signInService.signIn(kakaoCodeRequest);
        return new DataResponse<>(kakaoOauthResponse);
    }

    @PostMapping("/sign-up")
    public CustomResponse signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        SignUpResponse signUpResponseDto = signUpService.signUp(signUpRequest);
        return new DataResponse<>(signUpResponseDto);
    }

    @PostMapping("/sign-out")
    public CustomResponse signOut(Principal principal) {
        boolean status = authService.signOut(principal.getName());
        return new StatusResponse(status);
    }

    @PatchMapping("/quit")
    public CustomResponse quit(Principal principal) {
        boolean status = authService.quit(principal.getName());
        return new StatusResponse(status);
    }

    @PostMapping("/reissue")
    public CustomResponse reissueToken(Principal principal) {
        ReissueResponse reissueResponse = authService.reissue(principal.getName());
        return new DataResponse<>(reissueResponse);
    }
}
