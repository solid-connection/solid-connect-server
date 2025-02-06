package com.example.solidconnection.auth.client;

import com.example.solidconnection.auth.dto.oauth.AppleTokenDto;
import com.example.solidconnection.auth.dto.oauth.AppleUserInfoDto;
import com.example.solidconnection.config.client.AppleOAuthClientProperties;
import com.example.solidconnection.custom.exception.CustomException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.PublicKey;
import java.util.Objects;

import static com.example.solidconnection.custom.exception.ErrorCode.APPLE_AUTHORIZATION_FAILED;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_APPLE_ID_TOKEN;

/*
 * 애플 인증을 위한 OAuth2 클라이언트
 * https://developer.apple.com/documentation/signinwithapplerestapi/generate_and_validate_tokens
 * */
@Component
@RequiredArgsConstructor
public class AppleOAuthClient {

    private final RestTemplate restTemplate;
    private final AppleOAuthClientProperties properties;
    private final AppleOAuthClientSecretProvider clientSecretProvider;
    private final ApplePublicKeyProvider publicKeyProvider;

    public AppleUserInfoDto processOAuth(String code) {
        String idToken = requestIdToken(code);
        PublicKey applePublicKey = publicKeyProvider.getApplePublicKey(idToken);
        return new AppleUserInfoDto(parseEmailFromToken(applePublicKey, idToken));
    }

    public String requestIdToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> formData = buildFormData(code);

        try {
            ResponseEntity<AppleTokenDto> response = restTemplate.exchange(
                    properties.tokenUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(formData, headers),
                    AppleTokenDto.class
            );
            return Objects.requireNonNull(response.getBody()).idToken();
        } catch (Exception e) {
            throw new CustomException(APPLE_AUTHORIZATION_FAILED, e.getMessage());
        }
    }

    private MultiValueMap<String, String> buildFormData(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", properties.clientId());
        formData.add("client_secret", clientSecretProvider.generateClientSecret());
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", properties.redirectUrl());
        return formData;
    }

    private String parseEmailFromToken(PublicKey applePublicKey, String idToken) {
        try {
            return Jwts.parser()
                    .setSigningKey(applePublicKey)
                    .parseClaimsJws(idToken)
                    .getBody()
                    .get("email", String.class);
        } catch (Exception e) {
            throw new CustomException(INVALID_APPLE_ID_TOKEN);
        }
    }
}
