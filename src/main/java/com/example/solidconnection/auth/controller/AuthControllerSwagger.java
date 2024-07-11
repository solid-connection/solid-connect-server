package com.example.solidconnection.auth.controller;

import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.dto.SignUpResponse;
import com.example.solidconnection.auth.dto.kakao.FirstAccessResponse;
import com.example.solidconnection.auth.dto.kakao.KakaoCodeRequest;
import com.example.solidconnection.auth.dto.kakao.KakaoOauthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static com.example.solidconnection.config.swagger.SwaggerConfig.ACCESS_TOKEN;

@Tag(name = "Auth", description = "인증 API")
public interface AuthControllerSwagger {

    @Operation(
            summary = "카카오 OAuth 처리",
            requestBody = @RequestBody(
                    description = "클라이언트가 받아온 카카오 인증 코드",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KakaoCodeRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 성공 또는 회원가입을 위한 사용자 정보 불러오기 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(oneOf = {SignInResponse.class, FirstAccessResponse.class})
                            )
                    )
            }
    )
    ResponseEntity<KakaoOauthResponse> processKakaoOauth(@RequestBody KakaoCodeRequest kakaoCodeRequest);

    @Operation(
            summary = "회원가입",
            requestBody = @RequestBody(
                    description = "회원가입 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignUpRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원가입 성공, 엑세스 토큰과 리프레시 토큰 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SignUpResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest signUpRequest);

    @SecurityRequirements
    @SecurityRequirement(name = ACCESS_TOKEN)
    @Operation(
            summary = "로그아웃",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "로그아웃 성공"
                    )
            }
    )
    ResponseEntity<Void> signOut(Principal principal);

    @SecurityRequirements
    @SecurityRequirement(name = ACCESS_TOKEN)
    @Operation(
            summary = "회원 탈퇴",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원 탈퇴 성공"
                    )
            }
    )
    ResponseEntity<Void> quit(Principal principal);

    @SecurityRequirements
    @SecurityRequirement(name = ACCESS_TOKEN)
    @Operation(
            summary = "토큰 재발급",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "토큰 재발급 성공, 새 토큰 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ReissueResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<ReissueResponse> reissueToken(Principal principal);
}
