package com.example.solidconnection.admin.location.country.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCountryUpdateRequest(
        @NotBlank(message = "한글 국가명은 필수입니다")
        @Size(min = 1, max = 100, message = "한글 국가명은 1자 이상 100자 이하여야 합니다")
        String koreanName,

        @NotBlank(message = "지역 코드는 필수입니다")
        @Size(min = 1, max = 10, message = "지역 코드는 1자 이상 10자 이하여야 합니다")
        String regionCode
) {

}
