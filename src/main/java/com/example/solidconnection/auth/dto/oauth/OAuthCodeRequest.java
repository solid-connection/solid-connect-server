package com.example.solidconnection.auth.dto.oauth;

import jakarta.validation.constraints.NotBlank;

public record OAuthCodeRequest(

        @NotBlank(message = "인증 코드를 입력해주세요.")
        String code) {
}
