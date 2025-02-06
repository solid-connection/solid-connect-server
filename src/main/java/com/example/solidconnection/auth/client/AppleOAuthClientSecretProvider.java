package com.example.solidconnection.auth.client;

import com.example.solidconnection.config.client.AppleOAuthClientProperties;
import com.example.solidconnection.custom.exception.CustomException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.stream.Collectors;

import static com.example.solidconnection.custom.exception.ErrorCode.FAILED_TO_READ_APPLE_PRIVATE_KEY;

/*
 * 애플 OAuth 에 필요하 클라이언트 시크릿은 매번 동적으로 생성해야 한다.
 * 클라이언트 시크릿은 애플 개발자 계정에서 발급받은 개인키(*.p8)를 사용하여 JWT 를 생성한다.
 * https://developer.apple.com/documentation/accountorganizationaldatasharing/creating-a-client-secret
 * */
@Component
@RequiredArgsConstructor
public class AppleOAuthClientSecretProvider {

    private static final String KEY_ID_HEADER = "kid";
    private static final long TOKEN_DURATION = 1000 * 60; // 10min
    private static final String SECRET_KEY_PATH = "secret/AppleOAuthKey.p8";

    private final AppleOAuthClientProperties appleOAuthClientProperties;
    private PrivateKey privateKey;

    @PostConstruct
    private void initPrivateKey() {
        privateKey = readPrivateKey();
    }

    public String generateClientSecret() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + TOKEN_DURATION);

        return Jwts.builder()
                .setHeaderParam("alg", "ES256")
                .setHeaderParam(KEY_ID_HEADER, appleOAuthClientProperties.keyId())
                .setSubject(appleOAuthClientProperties.clientId())
                .setIssuer(appleOAuthClientProperties.teamId())
                .setAudience(appleOAuthClientProperties.clientSecretAudienceUrl())
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.ES256, privateKey)
                .compact();
    }

    private PrivateKey readPrivateKey() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(SECRET_KEY_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String secretKey = reader.lines().collect(Collectors.joining("\n"));
            byte[] encoded = Base64.decodeBase64(secretKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new CustomException(FAILED_TO_READ_APPLE_PRIVATE_KEY);
        }
    }
}
