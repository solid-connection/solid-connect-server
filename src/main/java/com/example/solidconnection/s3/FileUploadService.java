package com.example.solidconnection.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.solidconnection.custom.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.example.solidconnection.custom.exception.ErrorCode.S3_CLIENT_EXCEPTION;
import static com.example.solidconnection.custom.exception.ErrorCode.S3_SERVICE_EXCEPTION;

@Component
@EnableAsync
@Slf4j
public class FileUploadService {

    private final AmazonS3Client amazonS3;

    public FileUploadService(AmazonS3Client amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Async
    public void uploadFile(String bucket, String fileName, MultipartFile multipartFile) {
        // 메타데이터 생성
        String contentType = multipartFile.getContentType();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(multipartFile.getSize());

        try {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            log.info("이미지 업로드 정상적 완료 thread: {}", Thread.currentThread().getName());
        } catch (AmazonServiceException e) {
            log.error("이미지 업로드 중 s3 서비스 예외 발생 : {}", e.getMessage());
            throw new CustomException(S3_SERVICE_EXCEPTION);
        } catch (SdkClientException | IOException e) {
            log.error("이미지 업로드 중 s3 클라이언트 예외 발생 : {}", e.getMessage());
            throw new CustomException(S3_CLIENT_EXCEPTION);
        }
    }
}
