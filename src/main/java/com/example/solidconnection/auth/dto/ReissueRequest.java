package com.example.solidconnection.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(
        @NotBlank(message = "리프레시 토큰과 함께 요청해주세요.")
        String refreshToken) {

}
