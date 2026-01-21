package com.example.solidconnection.s3.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.s3.domain.UploadPath;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.dto.urlPrefixResponse;
import com.example.solidconnection.s3.service.S3Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/file")
@RestController
public class S3Controller {

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
            @RequestParam("file") MultipartFile imageFile
    ) {
        UploadedFileUrlResponse profileImageUrl = s3Service.uploadFile(imageFile, UploadPath.PROFILE);
        return ResponseEntity.ok(profileImageUrl);
    }

    @PostMapping("/profile/post")
    public ResponseEntity<UploadedFileUrlResponse> uploadPostProfileImage(
            @AuthorizedUser long siteUserId,
            @RequestParam("file") MultipartFile imageFile
    ) {
        UploadedFileUrlResponse profileImageUrl = s3Service.uploadFile(imageFile, UploadPath.PROFILE);
        s3Service.deleteExProfile(siteUserId);
        return ResponseEntity.ok(profileImageUrl);
    }

    @PostMapping("/gpa")
    public ResponseEntity<UploadedFileUrlResponse> uploadGpaImage(
            @RequestParam("file") MultipartFile imageFile
    ) {
        UploadedFileUrlResponse profileImageUrl = s3Service.uploadFile(imageFile, UploadPath.GPA);
        return ResponseEntity.ok(profileImageUrl);
    }

    @PostMapping("/language-test")
    public ResponseEntity<UploadedFileUrlResponse> uploadLanguageImage(
            @RequestParam("file") MultipartFile imageFile
    ) {
        UploadedFileUrlResponse profileImageUrl = s3Service.uploadFile(imageFile, UploadPath.LANGUAGE_TEST);
        return ResponseEntity.ok(profileImageUrl);
    }

    @PostMapping("/chat")
    public ResponseEntity<List<UploadedFileUrlResponse>> uploadChatFile(
            @RequestParam("files") List<MultipartFile> files
    ) {
        List<UploadedFileUrlResponse> chatImageUrls = s3Service.uploadFiles(files, UploadPath.CHAT);
        return ResponseEntity.ok(chatImageUrls);
    }

    @GetMapping("/s3-url-prefix")
    public ResponseEntity<urlPrefixResponse> getS3UrlPrefix() {
        return ResponseEntity.ok(new urlPrefixResponse(s3Default, s3Uploaded, cloudFrontDefault, cloudFrontUploaded));
    }
}
