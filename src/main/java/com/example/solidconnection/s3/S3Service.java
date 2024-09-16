package com.example.solidconnection.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.ImgType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static com.example.solidconnection.custom.exception.ErrorCode.FILE_NOT_EXIST;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_FILE_EXTENSIONS;
import static com.example.solidconnection.custom.exception.ErrorCode.NOT_ALLOWED_FILE_EXTENSIONS;
import static com.example.solidconnection.custom.exception.ErrorCode.S3_CLIENT_EXCEPTION;
import static com.example.solidconnection.custom.exception.ErrorCode.S3_SERVICE_EXCEPTION;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);
    private final AmazonS3Client amazonS3;
    private final SiteUserRepository siteUserRepository;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /*
     * 파일을 S3에 업로드한다.
     * - 파일이 존재하는지 검증한다.
     * - 파일 확장자가 허용된 확장자인지 검증한다.
     * - 파일에 대한 메타 데이터를 생성한다.
     * - 임의의 랜덤한 문자열로 파일 이름을 생성한다.
     * - S3에 파일을 업로드한다.
     * */
    public UploadedFileUrlResponse uploadFile(MultipartFile multipartFile, ImgType imageFile) {
        // 파일 검증
        validateImgFile(multipartFile);

        // 메타데이터 생성
        String contentType = multipartFile.getContentType();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(multipartFile.getSize());

        // 파일 이름 생성
        UUID randomUUID = UUID.randomUUID();
        String fileName = imageFile.getType() + "/" + randomUUID;

        try {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (AmazonServiceException e) {
            log.error("이미지 업로드 중 s3 서비스 예외 발생 : {}", e.getMessage());
            throw new CustomException(S3_SERVICE_EXCEPTION);
        } catch (SdkClientException | IOException e) {
            log.error("이미지 업로드 중 s3 클라이언트 예외 발생 : {}", e.getMessage());
            throw new CustomException(S3_CLIENT_EXCEPTION);
        }
        return new UploadedFileUrlResponse(fileName);
    }

    public List<UploadedFileUrlResponse> uploadFiles(List<MultipartFile> multipartFile, ImgType imageFile) {

        List<UploadedFileUrlResponse> uploadedFileUrlResponseList = new ArrayList<>();

        for (MultipartFile file : multipartFile) {
            // 파일 검증
            validateImgFile(file);

            // 메타데이터 생성
            String contentType = file.getContentType();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(file.getSize());

            // 파일 이름 생성
            UUID randomUUID = UUID.randomUUID();
            String fileName = imageFile.getType() + "/" + randomUUID;

            try {
                amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (AmazonServiceException e) {
                log.error("이미지 업로드 중 s3 서비스 예외 발생 : {}", e.getMessage());
                throw new CustomException(S3_SERVICE_EXCEPTION);
            } catch (SdkClientException | IOException e) {
                log.error("이미지 업로드 중 s3 클라이언트 예외 발생 : {}", e.getMessage());
                throw new CustomException(S3_CLIENT_EXCEPTION);
            }
            uploadedFileUrlResponseList.add(new UploadedFileUrlResponse(fileName));
        }

        return uploadedFileUrlResponseList;
    }

    private void validateImgFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(FILE_NOT_EXIST);
        }

        String fileName = Objects.requireNonNull(file.getOriginalFilename());
        String fileExtension = getFileExtension(fileName).toLowerCase();

        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp", "pdf", "word", "docx");
        if (!allowedExtensions.contains(fileExtension)) {
            throw new CustomException(NOT_ALLOWED_FILE_EXTENSIONS, "허용된 형식: " + allowedExtensions);
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new CustomException(INVALID_FILE_EXTENSIONS);
        }
        return fileName.substring(dotIndex + 1);
    }

    /*
     * 기존 파일을 삭제한다.
     * - 기존 파일의 key(S3파일명)를 찾는다.
     * - S3에서 파일을 삭제한다.
     * */
    public void deleteExProfile(String email) {
        String key = getExProfileImageUrl(email);
        deleteFile(key);
    }

    public void deletePostImage(String url) {
        deleteFile(url);
    }

    private void deleteFile(String fileName) {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
        } catch (AmazonServiceException e) {
            log.error("파일 삭제 중 s3 서비스 예외 발생 : {}", e.getMessage());
            throw new CustomException(S3_SERVICE_EXCEPTION);
        } catch (SdkClientException e) {
            log.error("파일 삭제 중 s3 클라이언트 예외 발생 : {}", e.getMessage());
            throw new CustomException(S3_CLIENT_EXCEPTION);
        }
    }

    private String getExProfileImageUrl(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        return siteUser.getProfileImageUrl();
    }
}
