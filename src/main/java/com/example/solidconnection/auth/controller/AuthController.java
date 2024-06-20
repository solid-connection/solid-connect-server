package com.example.solidconnection.auth.controller;

import com.example.solidconnection.auth.dto.ReissueResponseDto;
import com.example.solidconnection.auth.dto.SignUpRequestDto;
import com.example.solidconnection.auth.dto.SignUpResponseDto;
import com.example.solidconnection.auth.dto.kakao.KakaoCodeDto;
import com.example.solidconnection.auth.dto.kakao.KakaoOauthResponseDto;
import com.example.solidconnection.auth.service.AuthService;
import com.example.solidconnection.auth.service.KakaoOAuthService;
import com.example.solidconnection.custom.response.CustomResponse;
import com.example.solidconnection.custom.response.DataResponse;
import com.example.solidconnection.custom.response.StatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final AuthService authService;

    @PostMapping("/kakao")
    public CustomResponse processKakaoOauth(@RequestBody KakaoCodeDto kakaoCodeDto) {
        KakaoOauthResponseDto kakaoOauthResponseDto = kakaoOAuthService.processOauth(kakaoCodeDto.getCode());
        return new DataResponse<>(kakaoOauthResponseDto);
    }

    @PostMapping("/sign-up")
    public CustomResponse signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        SignUpResponseDto signUpResponseDto = authService.signUp(signUpRequestDto);
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
