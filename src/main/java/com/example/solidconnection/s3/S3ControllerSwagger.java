package com.example.solidconnection.s3;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

import static com.example.solidconnection.config.swagger.SwaggerConfig.ACCESS_TOKEN;

@Tag(name = "ImageUpload", description = "S3 파일 업로드 API")
public interface S3ControllerSwagger {

    @Operation(
            summary = "회원가입 전 프로필 이미지 업로드 - 프로필 이미지 설정",
            requestBody = @RequestBody(
                    description = "업로드할 프로필 이미지 파일",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = MultipartFile.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "프로필 이미지 업로드 성공, URL 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UploadedFileUrlResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<UploadedFileUrlResponse> uploadPreProfileImage(@RequestParam("file") MultipartFile imageFile);

    @SecurityRequirements
    @SecurityRequirement(name = ACCESS_TOKEN)
    @Operation(
            summary = "회원가입 후 프로필 이미지 업로드 - 프로필 이미지 수정",
            requestBody = @RequestBody(
                    description = "업로드할 프로필 이미지 파일",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = MultipartFile.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "프로필 이미지 업로드 성공 후 기존 이미지 삭제, URL 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UploadedFileUrlResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<UploadedFileUrlResponse> uploadPostProfileImage(@RequestParam("file") MultipartFile imageFile, Principal principal);

    @SecurityRequirements
    @SecurityRequirement(name = ACCESS_TOKEN)
    @Operation(
            summary = "GPA 증명서 이미지 업로드",
            requestBody = @RequestBody(
                    description = "업로드할 GPA 증명서 이미지 파일",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = MultipartFile.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "GPA 증명서 이미지 업로드 성공, URL 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UploadedFileUrlResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<UploadedFileUrlResponse> uploadGpaImage(@RequestParam("file") MultipartFile imageFile);

    @SecurityRequirements
    @SecurityRequirement(name = ACCESS_TOKEN)
    @Operation(
            summary = "어학 시험 증명서 이미지 업로드",
            requestBody = @RequestBody(
                    description = "업로드할 어학 시험 증명서 이미지 파일",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = MultipartFile.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "어학 시험 증명서 이미지 업로드 성공, URL 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UploadedFileUrlResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<UploadedFileUrlResponse> uploadLanguageImage(@RequestParam("file") MultipartFile imageFile);

    @SecurityRequirements
    @SecurityRequirement(name = ACCESS_TOKEN)
    @Operation(
            summary = "S3 url prefix 확인",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "S3 url prefix 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = urlPrefixResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<urlPrefixResponse> getS3UrlPrefix();
}
