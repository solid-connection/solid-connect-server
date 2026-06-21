package com.example.solidconnection.admin.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminHostUniversityCreateRequest(
        @NotBlank(message = "한글 대학명은 필수입니다")
        @Size(max = 100, message = "한글 대학명은 100자 이하여야 합니다")
        String koreanName,

        @NotBlank(message = "영문 대학명은 필수입니다")
        @Size(max = 200, message = "영문 대학명은 200자 이하여야 합니다")
        String englishName,

        @NotBlank(message = "표시 대학명은 필수입니다")
        @Size(max = 100, message = "표시 대학명은 100자 이하여야 합니다")
        String formatName,

        @Size(max = 500, message = "홈페이지 URL은 500자 이하여야 합니다")
        String homepageUrl,

        @Size(max = 500, message = "영어 강좌 URL은 500자 이하여야 합니다")
        String englishCourseUrl,

        @Size(max = 500, message = "숙소 URL은 500자 이하여야 합니다")
        String accommodationUrl,

        @Size(max = 1000, message = "상세 정보는 1000자 이하여야 합니다")
        String detailsForLocal,

        @NotBlank(message = "국가 코드는 필수입니다")
        String countryCode,

        @NotBlank(message = "지역 코드는 필수입니다")
        String regionCode
) {

}
