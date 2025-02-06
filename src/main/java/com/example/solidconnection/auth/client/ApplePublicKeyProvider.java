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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.solidconnection.custom.exception.ErrorCode.APPLE_ID_TOKEN_EXPIRED;
import static com.example.solidconnection.custom.exception.ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_APPLE_ID_TOKEN;
import static org.apache.tomcat.util.codec.binary.Base64.decodeBase64URLSafe;

/*
* idToken 검증을 위해서 애플의 공개키를 가져온다.
* - 애플 공개키는 주기적으로 바뀐다. 이를 효율적으로 관리하기 위해 캐싱한다.
* - idToken 의 헤더에 있는 kid 값에 해당하는 키가 캐싱되어있으면 그것을 반환한다.
* - 그렇지 않다면 공개키가 바뀌었다는 뜻이므로, JSON 형식의 공개키 목록을 받아오고 캐시를 갱신한다.
* https://developer.apple.com/documentation/signinwithapplerestapi/fetch_apple_s_public_key_for_verifying_token_signature
* */
@Component
@RequiredArgsConstructor
public class ApplePublicKeyProvider {

    private final AppleOAuthClientProperties properties;
    private final RestTemplate restTemplate;

    private final Map<String, PublicKey> applePublicKeyCache = new ConcurrentHashMap<>();

    public PublicKey getApplePublicKey(String idToken) {
        try {
            String kid = getKeyIdFromTokenHeader(idToken);
            if (applePublicKeyCache.containsKey(kid)) {
                return applePublicKeyCache.get(kid);
            }

            fetchApplePublicKeys();
            if (applePublicKeyCache.containsKey(kid)) {
                return applePublicKeyCache.get(kid);
            } else {
                throw new CustomException(APPLE_PUBLIC_KEY_NOT_FOUND);
            }
        } catch (ExpiredJwtException e) {
            throw new CustomException(APPLE_ID_TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new CustomException(INVALID_APPLE_ID_TOKEN);
        }
    }

    /*
    * idToken 은 JWS 이므로, 원칙적으로는 서명까지 검증되어야 parsing 이 가능하다
    * 하지만 이 시점에서는 서명(=공개키)을 알 수 없으므로, Jwt 를 직접 인코딩하여 헤더를 가져온다.
    * */
    private String getKeyIdFromTokenHeader(String idToken) throws JsonProcessingException {
        String[] jwtParts = idToken.split("\\.");
        if (jwtParts.length < 2) {
            throw new CustomException(INVALID_APPLE_ID_TOKEN);
        }
        String headerJson = new String(Base64.getUrlDecoder().decode(jwtParts[0]), StandardCharsets.UTF_8);
        return new ObjectMapper().readTree(headerJson).get("kid").asText();
    }

    private void fetchApplePublicKeys() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseEntity<String> response = restTemplate.getForEntity(properties.publicKeyUrl(), String.class);
        JsonNode jsonNode = objectMapper.readTree(response.getBody()).get("keys");

        applePublicKeyCache.clear();
        for (JsonNode key : jsonNode) {
            applePublicKeyCache.put(key.get("kid").asText(), generatePublicKey(key));
        }
    }

    private PublicKey generatePublicKey(JsonNode key) throws Exception {
        BigInteger modulus = new BigInteger(1, decodeBase64URLSafe(key.get("n").asText()));
        BigInteger exponent = new BigInteger(1, decodeBase64URLSafe(key.get("e").asText()));
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
