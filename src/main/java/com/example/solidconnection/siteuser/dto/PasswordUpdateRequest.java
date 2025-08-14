package com.example.solidconnection.siteuser.dto;

import com.example.solidconnection.auth.dto.validation.Password;
import com.example.solidconnection.siteuser.dto.validation.PasswordConfirmation;
import jakarta.validation.constraints.NotBlank;

@PasswordConfirmation
public record PasswordUpdateRequest(
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Password
        String newPassword,

        @NotBlank(message = "새 비밀번호를 다시 한번 입력해주세요.")
        String newPasswordConfirmation
) {

}
