package com.example.solidconnection.siteuser.controller;

import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.dto.MyPageUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static com.example.solidconnection.config.swagger.SwaggerConfig.ACCESS_TOKEN;

@Tag(name = "SiteUser", description = "사용자 API")
@SecurityRequirements
@SecurityRequirement(name = ACCESS_TOKEN)
public interface SiteUserControllerSwagger {

    @Operation(
            summary = "마이 페이지 페이지 정보 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "마이 페이지 정보 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MyPageResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<MyPageResponse> getMyPageInfo(Principal principal);

    @Operation(
            summary = "마이 페이지 정보 수정을 위한 데이터 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수정 가능한 정보 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MyPageUpdateResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<MyPageUpdateResponse> getMyPageInfoToUpdate(Principal principal);
}
