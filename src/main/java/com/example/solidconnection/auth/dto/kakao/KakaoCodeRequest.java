package com.example.solidconnection.auth.dto.kakao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "클라이언트에서 받은 카카오 코드")
public record KakaoCodeRequest(
        @Schema(description = "카카오 코드", example = "ABCD1234")
        String code) {
}
