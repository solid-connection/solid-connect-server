package com.example.solidconnection.s3.service;

import static com.example.solidconnection.common.exception.ErrorCode.FILE_NOT_EXIST;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_FILE_EXTENSIONS;
import static com.example.solidconnection.common.exception.ErrorCode.NOT_ALLOWED_FILE_EXTENSIONS;
import static com.example.solidconnection.common.exception.ErrorCode.S3_CLIENT_EXCEPTION;
import static com.example.solidconnection.common.exception.ErrorCode.S3_SERVICE_EXCEPTION;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.s3.domain.UploadPath;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final long MAX_FILE_SIZE_MB = 1024 * 1024 * 5;

    private final S3Client s3Client;
    private final SiteUserRepository siteUserRepository;
    private final FileUploadService fileUploadService;
    private final ThreadPoolTaskExecutor asyncExecutor;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /*
     * 파일을 S3에 업로드한다.
     * - 파일이 존재하는지 검증한다.
     * - 파일 확장자가 허용된 확장자인지 검증한다.
     * - 파일에 대한 메타 데이터를 생성한다.
     * - 임의의 랜덤한 문자열로 파일 이름을 생성한다.
     * - S3에 파일을 업로드한다.
     * - 5mb 이상의 파일은 /origin/ 경로로 업로드하여 lambda 함수로 리사이징 진행한다.
     * - 5mb 미만의 파일은 바로 업로드한다.
     * */
    public UploadedFileUrlResponse uploadFile(MultipartFile multipartFile, UploadPath uploadPath) {
        validateFile(multipartFile);
        UUID randomUUID = UUID.randomUUID();
        String extension = getFileExtension(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        String baseFileName = randomUUID + "." + extension;
        String fileName = uploadPath.getType() + "/" + baseFileName;
        final boolean isLargeFile = multipartFile.getSize() >= MAX_FILE_SIZE_MB && uploadPath != UploadPath.CHAT;

        final String originalPath = isLargeFile ? "original/" + fileName : fileName;
        final String returnPath = isLargeFile
                ? "resize/" + fileName.substring(0, fileName.lastIndexOf('.')) + ".webp"
                : fileName;

        fileUploadService.uploadFile(bucket, originalPath, multipartFile);

        return new UploadedFileUrlResponse(returnPath);
    }

    public List<UploadedFileUrlResponse> uploadFiles(List<MultipartFile> multipartFile, UploadPath uploadPath) {

        List<UploadedFileUrlResponse> uploadedFileUrlResponseList = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            UploadedFileUrlResponse uploadedFileUrlResponse = uploadFile(file, uploadPath);
            uploadedFileUrlResponseList.add(uploadedFileUrlResponse);
        }
        return uploadedFileUrlResponseList;
    }

    private void validateFile(MultipartFile file) {
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
    @Transactional
    public void deleteExProfile(long siteUserId) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        String key = siteUser.getProfileImageUrl();
        deleteFile(key);
    }

    public void deletePostImage(String url) {
        deleteFile(url);
    }

    private void deleteFile(String fileName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            throw new CustomException(S3_SERVICE_EXCEPTION);
        } catch (SdkException e) {
            throw new CustomException(S3_CLIENT_EXCEPTION);
        }
    }
}
