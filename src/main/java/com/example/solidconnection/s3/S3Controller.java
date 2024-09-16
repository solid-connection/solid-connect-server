package com.example.solidconnection.s3;

import com.example.solidconnection.type.ImgType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RequiredArgsConstructor
@RequestMapping("/file")
@RestController
public class S3Controller implements S3ControllerSwagger {

    private final S3Service s3Service;
    @Value("${cloud.aws.s3.url.default}")
    private String s3Default;
    @Value("${cloud.aws.s3.url.uploaded}")
    private String s3Uploaded;
    @Value("${cloud.aws.cloudFront.url.default}")
    private String cloudFrontDefault;
    @Value("${cloud.aws.cloudFront.url.uploaded}")
    private String cloudFrontUploaded;

    @PostMapping("/profile/pre")
    public ResponseEntity<UploadedFileUrlResponse> uploadPreProfileImage(
            @RequestParam("file") MultipartFile imageFile) {
        UploadedFileUrlResponse profileImageUrl = s3Service.uploadFile(imageFile, ImgType.PROFILE);
        return ResponseEntity.ok(profileImageUrl);
    }

    @PostMapping("/profile/post")
    public ResponseEntity<UploadedFileUrlResponse> uploadPostProfileImage(
            @RequestParam("file") MultipartFile imageFile, Principal principal) {
        UploadedFileUrlResponse profileImageUrl = s3Service.uploadFile(imageFile, ImgType.PROFILE);
        s3Service.deleteExProfile(principal.getName());
        return ResponseEntity.ok(profileImageUrl);
    }

    @PostMapping("/gpa")
    public ResponseEntity<UploadedFileUrlResponse> uploadGpaImage(
            @RequestParam("file") MultipartFile imageFile) {
        UploadedFileUrlResponse profileImageUrl = s3Service.uploadFile(imageFile, ImgType.GPA);
        return ResponseEntity.ok(profileImageUrl);
    }

    @PostMapping("/language-test")
    public ResponseEntity<UploadedFileUrlResponse> uploadLanguageImage(
            @RequestParam("file") MultipartFile imageFile) {
        UploadedFileUrlResponse profileImageUrl = s3Service.uploadFile(imageFile, ImgType.LANGUAGE_TEST);
        return ResponseEntity.ok(profileImageUrl);
    }

    @GetMapping("/s3-url-prefix")
    public ResponseEntity<urlPrefixResponse> getS3UrlPrefix() {
        return ResponseEntity.ok(new urlPrefixResponse(s3Default, s3Uploaded, cloudFrontDefault, cloudFrontUploaded));
    }
}
