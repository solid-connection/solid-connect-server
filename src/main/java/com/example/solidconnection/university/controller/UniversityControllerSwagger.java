package com.example.solidconnection.university.controller;

import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.LikeResultResponse;
import com.example.solidconnection.university.dto.UniversityDetailResponse;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import com.example.solidconnection.university.dto.UniversityRecommendsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

import static com.example.solidconnection.config.swagger.SwaggerConfig.ACCESS_TOKEN;

@Tag(name = "University", description = "대학 및 대학 지원을 위한 정보 API")
@SecurityRequirements
@SecurityRequirement(name = ACCESS_TOKEN)
public interface UniversityControllerSwagger {

    @Operation(
            summary = "대학 추천 목록 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "사용자별 개인화 된 대학 추천 목록 또는 일반 추천 목록 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UniversityRecommendsResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<UniversityRecommendsResponse> getUniversityRecommends(Principal principal);

    @Operation(
            summary = "좋아요한 대학 목록 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "사용자가 좋아요한 대학 목록 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = UniversityInfoForApplyPreviewResponse.class))
                            )
                    )
            }
    )
    ResponseEntity<List<UniversityInfoForApplyPreviewResponse>> getMyWishUniversity(Principal principal);

    @Operation(
            summary = "대학 좋아요 여부 확인",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "대학 좋아요 여부 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = IsLikeResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<IsLikeResponse> getIsLiked(Principal principal, @PathVariable Long universityInfoForApplyId);

    @Operation(
            summary = "대학 좋아요 하기",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "대학 좋아요 결과 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = LikeResultResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<LikeResultResponse> addWishUniversity(Principal principal, @PathVariable Long universityInfoForApplyId);

    @Operation(
            summary = "대학 상세 정보 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "대학 상세 정보 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UniversityDetailResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<UniversityDetailResponse> getUniversityDetails(@PathVariable Long universityInfoForApplyId);

    @Operation(
            summary = "대학 검색",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "검색 조건에 맞는 대학 목록 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = UniversityInfoForApplyPreviewResponse.class))
                            )
                    )
            }
    )
    ResponseEntity<List<UniversityInfoForApplyPreviewResponse>> searchUniversity(
            @RequestParam(required = false, defaultValue = "") String region,
            @RequestParam(required = false, defaultValue = "") List<String> keyword,
            @RequestParam(required = false, defaultValue = "") LanguageTestType testType,
            @RequestParam(required = false, defaultValue = "") String testScore);
}
