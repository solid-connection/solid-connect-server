package com.example.solidconnection.s3.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record UploadedFileUrlResponse(
        String fileUrl,
        @JsonIgnore String deletionKey) {

    public UploadedFileUrlResponse(String fileUrl) {
        this(fileUrl, fileUrl);
    }
}
