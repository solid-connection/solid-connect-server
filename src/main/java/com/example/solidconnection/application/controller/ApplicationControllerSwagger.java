package com.example.solidconnection.application.controller;

import com.example.solidconnection.application.dto.ApplicationSubmissionResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.ScoreRequest;
import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.application.dto.VerifyStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.security.Principal;

import static com.example.solidconnection.config.swagger.SwaggerConfig.ACCESS_TOKEN;

@Tag(name = "Application", description = "지원 정보 API")
@SecurityRequirements
@SecurityRequirement(name = ACCESS_TOKEN)
public interface ApplicationControllerSwagger {

    @Operation(
            summary = "대학 성적과 어학 성적 제출",
            requestBody = @RequestBody(
                    description = "대학 성적과 어학 성적",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ScoreRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "대학 성적과 어학 성적 제출 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ApplicationSubmissionResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<ApplicationSubmissionResponse> submitScore(Principal principal, @Valid @RequestBody ScoreRequest scoreRequest);

    @Operation(
            summary = "지망 대학 제출",
            requestBody = @RequestBody(
                    description = "지망 대학",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UniversityChoiceRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "지망 대학 제출 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ApplicationSubmissionResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<ApplicationSubmissionResponse> submitUniversityChoice(Principal principal, @Valid @RequestBody UniversityChoiceRequest universityChoiceRequest);

    @Operation(
            summary = "지원자 목록 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "지원자 목록 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ApplicationsResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<ApplicationsResponse> getApplicants(Principal principal, @RequestParam(required = false) String region, @RequestParam(required = false) String keyword);

    @Operation(
            summary = "성적 승인 상태 확인",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성적 승인 상태 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = VerifyStatusResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<VerifyStatusResponse> getApplicationVerifyStatus(Principal principal);
}
