package com.example.solidconnection.s3.domain;

import com.example.solidconnection.common.constant.FileConstants;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import java.util.List;
import lombok.Getter;

@Getter
public enum UploadPath {
    PROFILE("profile", true),
    GPA("gpa", false),
    LANGUAGE_TEST("language", false),
    COMMUNITY("community", true),
    NEWS("news", true),
    CHAT("chat/files", false),
    MENTOR_PROOF("mentor-proof", false),
    ADMIN_UNIVERSITY_LOGO("admin/logo", true),
    ADMIN_UNIVERSITY_BACKGROUND("admin/background", true)
    ;

    private final String type;
    private final boolean imageOnly;

    UploadPath(String type, boolean imageOnly) {
        this.type = type;
        this.imageOnly = imageOnly;
    }

    public boolean isResizable(long fileSize, String extension, long maxSizeBytes) {
        if (!isImage(extension)) {
            return false;
        }

        if (this == CHAT) {
            return false;
        }

        return fileSize >= maxSizeBytes;
    }

    public void validateExtension(String extension) {
        if (extension == null || !getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new CustomException(ErrorCode.NOT_ALLOWED_FILE_EXTENSIONS,
                                      "허용된 형식: " + getAllowedExtensionsMessage());
        }
    }

    public boolean isImage(String extension) {
        return extension != null && FileConstants.IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    public String getAllowedExtensionsMessage() {
        return String.join(", ", getAllowedExtensions());
    }

    private List<String> getAllowedExtensions() {
        if (imageOnly) {
            return FileConstants.IMAGE_EXTENSIONS;
        }
        return FileConstants.ALL_ALLOWED_EXTENSIONS;
    }
}
