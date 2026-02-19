package com.example.solidconnection.s3.domain;

import com.example.solidconnection.common.constant.FileConstants;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum UploadPath {
    PROFILE("profile"),
    GPA("gpa"),
    LANGUAGE_TEST("language"),
    COMMUNITY("community"),
    NEWS("news"),
    CHAT("chat/files"),
    MENTOR_PROOF("mentor-proof"),
    ;

    private final String type;

    UploadPath(String type) {
        this.type = type;
    }

    public boolean isResizable(long fileSize, String extension, long maxSizeBytes) {
        if (!isImage(extension)) return false;

        if (this == CHAT) return false;

        return fileSize >= maxSizeBytes;
    }
    public void validateExtension(String extension) {
        if (extension == null || !FileConstants.ALL_ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new CustomException(ErrorCode.NOT_ALLOWED_FILE_EXTENSIONS,
                                      "허용된 형식: " + getAllowedExtensionsMessage());
        }
    }

    public boolean isImage(String extension) {
        return extension != null && FileConstants.IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    public String getAllowedExtensionsMessage() {
        return String.join(", ", FileConstants.ALL_ALLOWED_EXTENSIONS);
    }
}
