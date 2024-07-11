package com.example.solidconnection.s3;

import com.example.solidconnection.type.ImgType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RequiredArgsConstructor
@RequestMapping("/file")
@RestController
public class S3Controller implements S3ControllerSwagger {

    private final S3Service s3Service;

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
}
