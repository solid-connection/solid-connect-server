package com.example.solidconnection.auth.client;

import com.example.solidconnection.auth.dto.kakao.KakaoTokenDto;
import com.example.solidconnection.auth.dto.kakao.KakaoUserInfoDto;
import com.example.solidconnection.custom.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import static com.example.solidconnection.custom.exception.ErrorCode.KAKAO_USER_INFO_FAIL;
import static com.example.solidconnection.custom.exception.ErrorCode.KAKAO_REDIRECT_URI_MISMATCH;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final RestTemplate restTemplate;

    @Value("${kakao.client_id}")
    private String clientId;
    @Value("${kakao.redirect_uri}")
    private String redirectUri;
    @Value("${kakao.token_url}")
    private String tokenUrl;
    @Value("${kakao.user_info_url}")
    private String userInfoUrl;

    /*
    * 클라이언트에서 사용자가 카카오 로그인을 하면, 클라이언트는 '카카오 인가 코드'를 받아, 서버에 넘겨준다.
    *   - https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-code
    * 서버는 카카오 인증 코드를 사용해 카카오 서버로부터 '카카오 토큰'을 받아온다.
    *   - https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-token
    * 그리고 카카오 엑세스 토큰으로 카카오 서버에 요청해 '카카오 사용자 정보'를 받아온다.
    *   - https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info
    * */
    public KakaoUserInfoDto processOauth(String code) {
        String kakaoAccessToken = getKakaoAccessToken(code);
        return getKakaoUserInfo(kakaoAccessToken);
    }

    // 카카오 토큰 요청
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

    // 카카오 엑세스 토큰 요청하는 URI 생성
    private String buildTokenUri(String code) {
        return UriComponentsBuilder.fromHttpUrl(tokenUrl)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", code)
                .toUriString();
    }

    // 카카오 사용자 정보 요청
    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        // 사용자의 정보 요청
        ResponseEntity<KakaoUserInfoDto> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                KakaoUserInfoDto.class
        );

        // 응답 예외처리
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new CustomException(KAKAO_USER_INFO_FAIL);
        }
    }
}
