package com.example.solidconnection.s3.domain;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;

public final class UploadDirectoryName {

    private UploadDirectoryName() {
    }

    public static String fromUniversityEnglishName(String englishName) {
        if (englishName == null || englishName.isBlank()) {
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

        return directoryName;
    }
}
