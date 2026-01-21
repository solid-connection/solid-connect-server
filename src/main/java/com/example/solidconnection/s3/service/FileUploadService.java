package com.example.solidconnection.s3.service;

import static com.example.solidconnection.common.exception.ErrorCode.S3_CLIENT_EXCEPTION;
import static com.example.solidconnection.common.exception.ErrorCode.S3_SERVICE_EXCEPTION;

import com.example.solidconnection.common.exception.CustomException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileUploadService {

    private final S3Client s3Client;

    @Async
    public void uploadFile(String bucket, String fileName, MultipartFile multipartFile) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(multipartFile.getContentType())
                    .contentLength(multipartFile.getSize())
                    .build();

            s3Client.putObject(putObjectRequest,
                               RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));

            log.info("파일 업로드 정상 완료 thread: {}", Thread.currentThread().getName());
        } catch (S3Exception e) {
            String errorMessage = (e.awsErrorDetails() != null)
                    ? e.awsErrorDetails().errorMessage()
                    : e.getMessage();
            log.error("S3 서비스 예외 발생 : {}", errorMessage);
            throw new CustomException(S3_SERVICE_EXCEPTION);
        } catch (SdkException | IOException e) {
            log.error("S3 클라이언트 또는 IO 예외 발생 : {}", e.getMessage());
            throw new CustomException(S3_CLIENT_EXCEPTION);
        }
    }
}
