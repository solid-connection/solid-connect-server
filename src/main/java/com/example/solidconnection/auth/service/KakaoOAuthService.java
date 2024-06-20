package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.SignInResponseDto;
import com.example.solidconnection.auth.dto.kakao.*;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

import static com.example.solidconnection.custom.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final RestTemplate restTemplate;
    private final TokenService tokenService;
    private final SiteUserRepository siteUserRepository;

    @Value("${kakao.client_id}")
    private String clientId;
    @Value("${kakao.redirect_uri}")
    private String redirectUri;
    @Value("${kakao.token_url}")
    private String tokenUrl;
    @Value("${kakao.user_info_url}")
    private String userInfoUrl;

    public KakaoOauthResponseDto processOauth(String code) throws CustomException {
        String kakaoAccessToken = getKakaoAccessToken(code);
        KakaoUserInfoDto kakaoUserInfoDto = getKakaoUserInfo(kakaoAccessToken);
        String email = kakaoUserInfoDto.getKakaoAccount().getEmail();
        boolean isAlreadyRegistered = siteUserRepository.existsByEmail(email);
        if (isAlreadyRegistered) {
            resetQuitedAt(email);
            return kakaoSignIn(email);
        }
        String kakaoOauthToken = tokenService.generateToken(email, TokenType.KAKAO_OAUTH);
        return FirstAccessResponseDto.fromKakaoUserInfo(kakaoUserInfoDto, kakaoOauthToken);
    }

    private String getKakaoAccessToken(String code) {
        // 카카오 엑세스 토큰 요청
        try {
            ResponseEntity<KakaoTokenDto> response = restTemplate.exchange(
                    buildTokenUri(code),
                    HttpMethod.POST,
                    null,
                    KakaoTokenDto.class
            );
            return Objects.requireNonNull(response.getBody()).getAccessToken();
        } catch (Exception e) {
            if (e.getMessage().contains("KOE303")) {
                throw new CustomException(REDIRECT_URI_MISMATCH);
            }
            throw new CustomException(INVALID_KAKAO_AUTH_CODE);
        }
    }

    // 카카오에게 엑세스 토큰 발급 요청하는 URI 생성
    private String buildTokenUri(String code) {
        return UriComponentsBuilder.fromHttpUrl(tokenUrl)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", code)
                .toUriString();
    }

    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) {
        // 카카오 엑세스 토큰을 헤더에 담은 HttpEntity
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // 사용자의 정보 요청
        ResponseEntity<KakaoUserInfoDto> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                entity,
                KakaoUserInfoDto.class
        );

        // 응답 예외처리
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new CustomException(KAKAO_USER_INFO_FAIL);
        }
    }

    private SignInResponseDto kakaoSignIn(String email) {
        String accessToken = tokenService.generateToken(email, TokenType.ACCESS);
        String refreshToken = tokenService.generateToken(email, TokenType.REFRESH);
        tokenService.saveToken(refreshToken, TokenType.REFRESH);
        return SignInResponseDto.builder()
                .registered(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void resetQuitedAt(String email) {
        SiteUser siteUser = siteUserRepository.findByEmail(email).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        siteUser.setQuitedAt(null);
    }
}
