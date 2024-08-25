package com.example.solidconnection.siteuser.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record NicknameUpdateRequest(
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Schema(description = "변경할 닉네임", example = "NewNickname")
        String nickname
) {
}
