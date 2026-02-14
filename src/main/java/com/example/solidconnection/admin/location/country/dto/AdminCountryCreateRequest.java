package com.example.solidconnection.admin.location.country.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCountryCreateRequest(
        @NotBlank(message = "국가 코드는 필수입니다")
        @Size(min = 2, max = 2, message = "국가 코드는 2자여야 합니다")
        String code,

        @NotBlank(message = "한글 국가명은 필수입니다")
        @Size(min = 1, max = 100, message = "한글 국가명은 1자 이상 100자 이하여야 합니다")
        String koreanName,

        @NotBlank(message = "지역 코드는 필수입니다")
        @Size(min = 1, max = 10, message = "지역 코드는 1자 이상 10자 이하여야 합니다")
        String regionCode
) {

}
