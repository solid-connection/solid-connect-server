package com.example.solidconnection.siteuser.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordUpdateRequest(
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        // @Password // todo: #435 merge 후
        String newPassword,

        @NotBlank(message = "새 비밀번호를 다시 한번 입력해주세요.")
        String confirmNewPassword
) {

}
