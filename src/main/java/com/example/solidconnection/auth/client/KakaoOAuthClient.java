package com.example.solidconnection.auth.client;

import com.example.solidconnection.auth.dto.oauth.KakaoTokenDto;
import com.example.solidconnection.auth.dto.oauth.KakaoUserInfoDto;
import com.example.solidconnection.config.client.KakaoOAuthClientProperties;
import com.example.solidconnection.custom.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_OR_EXPIRED_KAKAO_AUTH_CODE;
import static com.example.solidconnection.custom.exception.ErrorCode.KAKAO_REDIRECT_URI_MISMATCH;
import static com.example.solidconnection.custom.exception.ErrorCode.KAKAO_USER_INFO_FAIL;

/*
 * 카카오 인증을 위한 OAuth2 클라이언트
 * https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-code
 * https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-token
 * https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info
 * */
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final RestTemplate restTemplate;
    private final KakaoOAuthClientProperties kakaoOAuthClientProperties;

    public KakaoUserInfoDto getUserInfo(String code) {
        String kakaoAccessToken = getKakaoAccessToken(code);
        return getKakaoUserInfo(kakaoAccessToken);
    }

    private String getKakaoAccessToken(String code) {
        try {
            ResponseEntity<KakaoTokenDto> response = restTemplate.exchange(
                    buildTokenUri(code),
                    HttpMethod.POST,
                    null,
                    KakaoTokenDto.class
            );
            return Objects.requireNonNull(response.getBody()).accessToken();
        } catch (Exception e) {
            if (e.getMessage().contains("KOE303")) {
                throw new CustomException(KAKAO_REDIRECT_URI_MISMATCH);
            }
            if (e.getMessage().contains("KOE320")) {
                throw new CustomException(INVALID_OR_EXPIRED_KAKAO_AUTH_CODE);
            }
            throw new CustomException(INVALID_OR_EXPIRED_KAKAO_AUTH_CODE);
        }
    }

    private String buildTokenUri(String code) {
        return UriComponentsBuilder.fromHttpUrl(kakaoOAuthClientProperties.tokenUrl())
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", kakaoOAuthClientProperties.clientId())
                .queryParam("redirect_uri", kakaoOAuthClientProperties.redirectUrl())
                .queryParam("code", code)
                .toUriString();
    }

    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<KakaoUserInfoDto> response = restTemplate.exchange(
                kakaoOAuthClientProperties.userInfoUrl(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                KakaoUserInfoDto.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new CustomException(KAKAO_USER_INFO_FAIL);
        }
    }
}
