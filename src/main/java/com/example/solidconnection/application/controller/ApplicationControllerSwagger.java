package com.example.solidconnection.application.controller;

import com.example.solidconnection.application.dto.ApplicationsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

import static com.example.solidconnection.config.swagger.SwaggerConfig.ACCESS_TOKEN;

@Tag(name = "Application", description = "지원 정보 API")
@SecurityRequirements
@SecurityRequirement(name = ACCESS_TOKEN)
public interface ApplicationControllerSwagger {
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
}
