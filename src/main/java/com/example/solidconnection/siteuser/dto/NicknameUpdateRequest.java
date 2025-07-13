package com.example.solidconnection.siteuser.dto;

import jakarta.validation.constraints.NotBlank;

public record NicknameUpdateRequest(
        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickname
) {

}
