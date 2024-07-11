package com.example.solidconnection.s3;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업로드된 파일의 URL 응답")
public record UploadedFileUrlResponse(
        @Schema(description = "파일 URL", example = "http://example.com/uploads/profile.jpg")
        String fileUrl) {
}
