package com.example.solidconnection.s3;

public record urlPrefixResponse(
        String s3Default,
        String s3Uploaded,
        String cloudFrontDefault,
        String cloudFrontUploaded
) {
}
