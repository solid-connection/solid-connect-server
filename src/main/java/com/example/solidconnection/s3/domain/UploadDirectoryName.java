package com.example.solidconnection.s3.domain;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class UploadDirectoryName {

    private static final int HASH_PREFIX_LENGTH = 12;

    private UploadDirectoryName() {
    }

    public static String fromUniversityNames(String englishName, String koreanName) {
        if (englishName == null || englishName.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (koreanName == null || koreanName.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        String directoryName = englishName.trim()
                .toLowerCase()
                .replaceAll("\\s*&\\s*", "_and_")
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_")
                .replaceAll("[^a-z0-9_-]", "");

        if (directoryName.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        return directoryName + "_" + hash(koreanName.trim());
    }

    private static String hash(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, HASH_PREFIX_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new CustomException(ErrorCode.NOT_DEFINED_ERROR);
        }
    }
}
