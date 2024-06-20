package com.example.solidconnection.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.ImgType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.example.solidconnection.custom.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class S3Service {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client amazonS3;
    private final SiteUserRepository siteUserRepository;
    
    public UploadedFileURLDto uploadFile(MultipartFile multipartFile, ImgType imageFile) {
        validateImgFile(multipartFile);
        String contentType = multipartFile.getContentType();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(multipartFile.getSize());

        UUID randomUUID = UUID.randomUUID();
        String fileName = imageFile.getType() + "/"+ randomUUID;

        try {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new CustomException(S3_SERVICE_EXCEPTION);
        } catch (SdkClientException | IOException e) {
            e.printStackTrace();
            throw new CustomException(S3_CLIENT_EXCEPTION);
        }

        return new UploadedFileURLDto(amazonS3.getUrl(bucket, fileName).toString());
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

    public void deleteExProfile(String email){
        String key = getExProfileImageUrl(email);
        deleteFile(key);
    }

    private void deleteFile(String fileName) {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new CustomException(S3_SERVICE_EXCEPTION);
        } catch (SdkClientException e) {
            e.printStackTrace();
            throw new CustomException(S3_CLIENT_EXCEPTION);
        }
    }

    private String getExProfileImageUrl(String email){
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        String fileName = siteUser.getProfileImageUrl();
        int domainStartIndex = fileName.indexOf(".com");
        return fileName.substring(domainStartIndex + 5);
    }
}
