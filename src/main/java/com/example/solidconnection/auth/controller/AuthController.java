package com.example.solidconnection.auth.controller;

import com.example.solidconnection.auth.dto.ReissueResponseDto;
import com.example.solidconnection.auth.dto.SignUpRequestDto;
import com.example.solidconnection.auth.dto.SignUpResponseDto;
import com.example.solidconnection.auth.dto.kakao.KakaoCodeDto;
import com.example.solidconnection.auth.dto.kakao.KakaoOauthResponseDto;
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

    @PostMapping("/kakao") //TODO: 추후 OAuth 추가를 염두에 둔다면 "/sign-in/kakao" 로 바꿔야 할 것 같다.
    public CustomResponse processKakaoOauth(@RequestBody KakaoCodeDto kakaoCodeDto) {
        KakaoOauthResponseDto kakaoOauthResponseDto = signInService.signIn(kakaoCodeDto);
        return new DataResponse<>(kakaoOauthResponseDto);
    }

    @PostMapping("/sign-up")
    public CustomResponse signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        SignUpResponseDto signUpResponseDto = signUpService.signUp(signUpRequestDto);
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
        ReissueResponseDto reissueResponseDto = authService.reissue(principal.getName());
        return new DataResponse<>(reissueResponseDto);
    }
}
