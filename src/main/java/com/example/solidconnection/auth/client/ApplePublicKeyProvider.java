package com.example.solidconnection.auth.client;

import com.example.solidconnection.config.client.AppleOAuthClientProperties;
import com.example.solidconnection.custom.exception.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import static com.example.solidconnection.custom.exception.ErrorCode.APPLE_ID_TOKEN_EXPIRED;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_APPLE_ID_TOKEN;
import static org.apache.tomcat.util.codec.binary.Base64.decodeBase64URLSafe;

/*
* idToken 검증을 위해서 애플의 공개키를 가져온다.
* - 애플 공개키는 주기적으로 바뀌므로, 항상 새롭게 받아와야 한다.
* - 애플 공개키를 받아오는 url 에 요청하여 JSON 형식의 공개키 목록을 받아온다.
* - 이중에서 idToken 의 헤더에 있는 kid 값과 일치하는 공개키로 PublicKey 객체를 생성한다.
* https://developer.apple.com/documentation/signinwithapplerestapi/fetch_apple_s_public_key_for_verifying_token_signature
* */
@Component
@RequiredArgsConstructor
public class ApplePublicKeyProvider {

    private final AppleOAuthClientProperties properties;
    private final RestTemplate restTemplate;

    public PublicKey getApplePublicKey(String idToken) {
        try {
            String kid = getKeyIdFromTokenHeader(idToken);
            JsonNode appleKeys = fetchApplePublicKeys();
            return getMatchingPublicKey(appleKeys, kid);
        } catch (ExpiredJwtException e) {
            throw new CustomException(APPLE_ID_TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new CustomException(INVALID_APPLE_ID_TOKEN);
        }
    }

    private String getKeyIdFromTokenHeader(String idToken) throws JsonProcessingException {
        String[] jwtParts = idToken.split("\\.");
        if (jwtParts.length < 2) {
            throw new CustomException(INVALID_APPLE_ID_TOKEN);
        }
        String headerJson = new String(Base64.getUrlDecoder().decode(jwtParts[0]), StandardCharsets.UTF_8);
        return new ObjectMapper().readTree(headerJson).get("kid").asText();
    }

    private JsonNode fetchApplePublicKeys() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseEntity<String> response = restTemplate.getForEntity(properties.publicKeyUrl(), String.class);
        return objectMapper.readTree(response.getBody()).get("keys");
    }

    private PublicKey getMatchingPublicKey(JsonNode appleKeys, String kid) throws Exception {
        for (JsonNode key : appleKeys) {
            if (key.get("kid").asText().equals(kid)) {
                return generatePublicKey(key);
            }
        }
        throw new CustomException(INVALID_APPLE_ID_TOKEN);
    }

    private PublicKey generatePublicKey(JsonNode key) throws Exception {
        BigInteger modulus = new BigInteger(1, decodeBase64URLSafe(key.get("n").asText()));
        BigInteger exponent = new BigInteger(1, decodeBase64URLSafe(key.get("e").asText()));
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
